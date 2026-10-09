package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.agendamento.HorariosClinicaResponse;
import br.com.fiap.java_afetto.dto.clinica.ClinicaLista;
import br.com.fiap.java_afetto.dto.clinica.ClinicaResponse;
import br.com.fiap.java_afetto.mapper.ClinicaMapper;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.clinica.Clinica;
import br.com.fiap.java_afetto.model.clinica.ClinicaFavorita;
import br.com.fiap.java_afetto.model.endereco.Endereco;
import br.com.fiap.java_afetto.repository.AvaliacaoRepository;
import br.com.fiap.java_afetto.repository.AvaliacaoRepository.ResumoAvaliacao;
import br.com.fiap.java_afetto.repository.ClinicaFavoritaRepository;
import br.com.fiap.java_afetto.repository.ClinicaRepository;
import br.com.fiap.java_afetto.repository.ClinicaVeterinarioRepository;
import br.com.fiap.java_afetto.repository.ExpedienteRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Clínicas parceiras.
 *
 * "Perto de você": até 5 km do ponto de referência. O ponto é a localização
 * enviada pelo app (?latitude=&longitude=) ou, sem ela, as coordenadas do
 * endereço do usuário. Sem nenhuma coordenada, "perto" é estar na mesma
 * cidade do endereço cadastrado. A distância usa o cálculo de Haversine que
 * já existia no DistanciaService.
 *
 * Favoritas: cada usuário tem as suas (TBL_CLINICA_FAVORITA).
 * Nota média e total de avaliações vêm de TBL_AVALIACAO.
 */
@Service
public class ClinicaService {

    private static final double RAIO_PERTO_KM = 5.0;

    private final ClinicaRepository clinicaRepository;
    private final ClinicaVeterinarioRepository clinicaVeterinarioRepository;
    private final ClinicaFavoritaRepository clinicaFavoritaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ExpedienteRepository expedienteRepository;
    private final AgendaService agendaService;
    private final ClinicaMapper clinicaMapper;
    private final UsuarioLogadoService usuarioLogadoService;
    private final DistanciaService distanciaService;
    private final Clock relogio;

    public ClinicaService(
            ClinicaRepository clinicaRepository,
            ClinicaVeterinarioRepository clinicaVeterinarioRepository,
            ClinicaFavoritaRepository clinicaFavoritaRepository,
            AvaliacaoRepository avaliacaoRepository,
            ExpedienteRepository expedienteRepository,
            AgendaService agendaService,
            ClinicaMapper clinicaMapper,
            UsuarioLogadoService usuarioLogadoService,
            DistanciaService distanciaService,
            Clock relogio
    ) {
        this.clinicaRepository = clinicaRepository;
        this.clinicaVeterinarioRepository = clinicaVeterinarioRepository;
        this.clinicaFavoritaRepository = clinicaFavoritaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.expedienteRepository = expedienteRepository;
        this.agendaService = agendaService;
        this.clinicaMapper = clinicaMapper;
        this.usuarioLogadoService = usuarioLogadoService;
        this.distanciaService = distanciaService;
        this.relogio = relogio;
    }

    public Page<ClinicaLista> read(
            Pageable pageable,
            String nome,
            BigDecimal latitude,
            BigDecimal longitude,
            boolean apenasFavoritas
    ) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        Referencia referencia = referenciaDoUsuario(usuario, latitude, longitude);
        Set<UUID> favoritas = idsDasFavoritas(usuario);
        Map<UUID, ResumoAvaliacao> avaliacoes = avaliacaoRepository
                .resumirPorClinica()
                .stream()
                .collect(Collectors.toMap(ResumoAvaliacao::getIdClinica, resumo -> resumo));

        List<Clinica> clinicas = (nome == null || nome.isBlank())
                ? clinicaRepository.findAll()
                : clinicaRepository.findByNomeContainingIgnoreCase(nome.trim());

        // A distância é calculada aqui, então a ordem (mais perto primeiro)
        // e a página também são montadas aqui
        List<ClinicaLista> ordenadas = clinicas.stream()
                .filter(clinica -> !apenasFavoritas || favoritas.contains(clinica.getId()))
                .map(clinica -> clinicaMapper.clinicaToResponseLista(
                        clinica,
                        dadosCalculados(
                                clinica,
                                referencia,
                                favoritas.contains(clinica.getId()),
                                avaliacoes.get(clinica.getId())
                        )
                ))
                .sorted(Comparator
                        .comparing(ClinicaLista::distanciaKm, Comparator.nullsLast(Comparator.<Double>naturalOrder()))
                        .thenComparing(ClinicaLista::perto, Comparator.reverseOrder())
                        .thenComparing(ClinicaLista::nome))
                .toList();

        int inicio = (int) Math.min(pageable.getOffset(), ordenadas.size());
        int fim = Math.min(inicio + pageable.getPageSize(), ordenadas.size());

        return new PageImpl<>(ordenadas.subList(inicio, fim), pageable, ordenadas.size());
    }

    public ClinicaResponse read(
            UUID id,
            BigDecimal latitude,
            BigDecimal longitude
    ) {

        Clinica clinica = buscarClinica(id);

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        Referencia referencia = referenciaDoUsuario(usuario, latitude, longitude);

        return clinicaMapper.clinicaToResponse(
                clinica,
                dadosCalculados(
                        clinica,
                        referencia,
                        clinicaFavoritaRepository.existsByUsuario_IdAndClinica_Id(usuario.getId(), clinica.getId()),
                        avaliacaoRepository.resumirDaClinica(clinica.getId()).orElse(null)
                ),
                clinicaVeterinarioRepository.findByClinica_IdAndAtivoTrue(clinica.getId()),
                expedienteRepository.findByClinica_Id(clinica.getId())
        );
    }

    // Horários livres para agendar nos próximos dias (padrão: 7 dias a partir de hoje)
    public HorariosClinicaResponse horarios(UUID idClinica, LocalDate inicio, int dias) {

        Clinica clinica = buscarClinica(idClinica);

        return clinicaMapper.horariosToResponse(
                clinica,
                AgendaService.DURACAO_MINUTOS,
                agendaService.horariosLivres(clinica, inicio, dias)
        );
    }

    // Marcar de novo uma clínica que já é favorita não faz nada
    public void favoritar(UUID idClinica) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        Clinica clinica = buscarClinica(idClinica);

        if (clinicaFavoritaRepository.existsByUsuario_IdAndClinica_Id(usuario.getId(), clinica.getId())) {
            return;
        }

        ClinicaFavorita favorita = new ClinicaFavorita();

        favorita.setUsuario(usuario);
        favorita.setClinica(clinica);
        favorita.setDataCriacao(LocalDateTime.now(relogio));

        clinicaFavoritaRepository.save(favorita);
    }

    // Desmarcar uma clínica que não é favorita também não faz nada
    public void desfavoritar(UUID idClinica) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        Clinica clinica = buscarClinica(idClinica);

        clinicaFavoritaRepository
                .findByUsuario_IdAndClinica_Id(usuario.getId(), clinica.getId())
                .ifPresent(clinicaFavoritaRepository::delete);
    }

    private Clinica buscarClinica(UUID id) {

        return clinicaRepository
                .findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Clínica não encontrada"
                        )
                );
    }

    private ClinicaMapper.DadosCalculados dadosCalculados(
            Clinica clinica,
            Referencia referencia,
            boolean favorita,
            ResumoAvaliacao avaliacoes
    ) {

        Double distancia = distanciaKm(clinica, referencia);

        return new ClinicaMapper.DadosCalculados(
                distancia,
                perto(clinica, distancia, referencia),
                favorita,
                avaliacoes != null ? Math.round(avaliacoes.getNotaMedia() * 10.0) / 10.0 : null,
                avaliacoes != null ? avaliacoes.getQuantidade() : 0
        );
    }

    private Set<UUID> idsDasFavoritas(Usuario usuario) {

        return clinicaFavoritaRepository
                .findByUsuario_Id(usuario.getId())
                .stream()
                .map(favorita -> favorita.getClinica().getId())
                .collect(Collectors.toSet());
    }

    // De onde medir: localização do app > coordenadas do endereço do usuário > nenhuma
    private Referencia referenciaDoUsuario(Usuario usuario, BigDecimal latitude, BigDecimal longitude) {

        Endereco endereco = usuario.getEndereco();
        String cidade = endereco != null ? cidadeDo(endereco) : null;

        if (latitude != null && longitude != null) {
            return new Referencia(latitude, longitude, cidade);
        }

        if (endereco != null && endereco.getLatitude() != null && endereco.getLongitude() != null) {
            return new Referencia(endereco.getLatitude(), endereco.getLongitude(), cidade);
        }

        return new Referencia(null, null, cidade);
    }

    private Double distanciaKm(Clinica clinica, Referencia referencia) {

        Endereco endereco = clinica.getEndereco();

        if (referencia.latitude() == null
                || endereco.getLatitude() == null
                || endereco.getLongitude() == null) {
            return null;
        }

        double km = distanciaService.calcularDistancia(
                referencia.latitude(), referencia.longitude(),
                endereco.getLatitude(), endereco.getLongitude()
        );

        return Math.round(km * 10.0) / 10.0;
    }

    private boolean perto(Clinica clinica, Double distanciaKm, Referencia referencia) {

        if (distanciaKm != null) {
            return distanciaKm <= RAIO_PERTO_KM;
        }

        return referencia.cidade() != null
                && normalizar(referencia.cidade()).equals(normalizar(cidadeDo(clinica.getEndereco())));
    }

    private String cidadeDo(Endereco endereco) {
        return endereco.getLogradouro().getBairro().getCidade().getNome();
    }

    // "São Paulo" e "sao paulo" contam como a mesma cidade
    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private record Referencia(BigDecimal latitude, BigDecimal longitude, String cidade) {
    }
}
