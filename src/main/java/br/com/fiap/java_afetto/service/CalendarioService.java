package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.calendario.CalendarioResponse;
import br.com.fiap.java_afetto.dto.calendario.EventoCalendario;
import br.com.fiap.java_afetto.dto.calendario.TipoEventoCalendario;
import br.com.fiap.java_afetto.exception.RegraNegocioException;
import br.com.fiap.java_afetto.model.Historico;
import br.com.fiap.java_afetto.model.Remedio;
import br.com.fiap.java_afetto.model.StatusHistorico;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.Vacina;
import br.com.fiap.java_afetto.model.pet.Pet;
import br.com.fiap.java_afetto.repository.HistoricoRepository;
import br.com.fiap.java_afetto.repository.RemedioRepository;
import br.com.fiap.java_afetto.repository.VacinaRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import br.com.fiap.java_afetto.util.FormatoData;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Calendário do tutor: junta vacinas, remédios e consultas dos pets dele
 * num período (padrão: o mês atual; no máximo 1 ano). Consultas canceladas
 * não aparecem. Remédio vira um evento por dia de tratamento; sem data de
 * fim, segue até o fim do período.
 */
@Service
public class CalendarioService {

    private static final int MAX_DIAS = 366;

    private final VacinaRepository vacinaRepository;
    private final RemedioRepository remedioRepository;
    private final HistoricoRepository historicoRepository;
    private final UsuarioLogadoService usuarioLogadoService;
    private final ConsultaService consultaService;
    private final Clock relogio;

    public CalendarioService(
            VacinaRepository vacinaRepository,
            RemedioRepository remedioRepository,
            HistoricoRepository historicoRepository,
            UsuarioLogadoService usuarioLogadoService,
            ConsultaService consultaService,
            Clock relogio
    ) {
        this.vacinaRepository = vacinaRepository;
        this.remedioRepository = remedioRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioLogadoService = usuarioLogadoService;
        this.consultaService = consultaService;
        this.relogio = relogio;
    }

    public CalendarioResponse read(LocalDate inicio, LocalDate fim, UUID idPet) {

        LocalDate de = inicio != null ? inicio : LocalDate.now(relogio).withDayOfMonth(1);
        LocalDate ate = fim != null ? fim : de.withDayOfMonth(de.lengthOfMonth());

        if (ate.isBefore(de)) {
            throw new RegraNegocioException("A data final não pode ser antes da inicial");
        }

        if (ChronoUnit.DAYS.between(de, ate) >= MAX_DIAS) {
            throw new RegraNegocioException("O período pode ter no máximo 1 ano");
        }

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        // Filtrar por um pet que não é do usuário responde 404
        if (idPet != null) {
            usuarioLogadoService.buscarPetDoUsuario(idPet);
        }

        List<EventoCalendario> eventos = new ArrayList<>();

        for (Vacina vacina : vacinaRepository.findByPet_Usuario_Id(usuario.getId())) {

            if (!doFiltro(vacina.getPet(), idPet)) {
                continue;
            }

            if (dentro(vacina.getDataAplicacao(), de, ate)) {
                eventos.add(evento(TipoEventoCalendario.VACINA, vacina.getNomeVacina(), "Vacina aplicada",
                        vacina.getDataAplicacao(), null, null, vacina.getId(), vacina.getPet()));
            }

            if (dentro(vacina.getProximaDose(), de, ate)) {
                eventos.add(evento(TipoEventoCalendario.PROXIMA_DOSE, vacina.getNomeVacina(), "Próxima dose",
                        vacina.getProximaDose(), null, null, vacina.getId(), vacina.getPet()));
            }
        }

        for (Remedio remedio : remedioRepository.findByPet_Usuario_Id(usuario.getId())) {

            if (!doFiltro(remedio.getPet(), idPet)) {
                continue;
            }

            LocalDate primeiroDia = remedio.getDataInicio().isAfter(de) ? remedio.getDataInicio() : de;
            LocalDate ultimoDia = remedio.getDataFim() != null && remedio.getDataFim().isBefore(ate)
                    ? remedio.getDataFim()
                    : ate;

            for (LocalDate dia = primeiroDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
                eventos.add(evento(TipoEventoCalendario.REMEDIO, remedio.getNomeRemedio(), detalheDoRemedio(remedio),
                        dia, null, null, remedio.getId(), remedio.getPet()));
            }
        }

        for (Historico consulta : historicoRepository.findByPet_Usuario_Id(usuario.getId())) {

            if (!doFiltro(consulta.getPet(), idPet)
                    || consulta.getStatus() == StatusHistorico.CANCELADO
                    || !dentro(consulta.getData(), de, ate)) {
                continue;
            }

            StatusHistorico status = consultaService.statusAtual(consulta);

            eventos.add(evento(TipoEventoCalendario.CONSULTA, consulta.getDescricao(), detalheDaConsulta(consulta),
                    consulta.getData(),
                    consulta.getHora() != null ? FormatoData.hora(consulta.getHora()) : null,
                    status != null ? status.name() : null,
                    consulta.getId(), consulta.getPet()));
        }

        // Por dia; dentro do dia, os sem horário primeiro, depois pela hora
        eventos.sort(Comparator
                .comparing(EventoCalendario::data)
                .thenComparing(EventoCalendario::hora, Comparator.nullsFirst(Comparator.<String>naturalOrder()))
                .thenComparing(EventoCalendario::tipo)
                .thenComparing(EventoCalendario::titulo));

        return new CalendarioResponse(de, ate, eventos.size(), eventos);
    }

    private boolean doFiltro(Pet pet, UUID idPet) {
        return idPet == null || pet.getId().equals(idPet);
    }

    private boolean dentro(LocalDate data, LocalDate de, LocalDate ate) {
        return data != null && !data.isBefore(de) && !data.isAfter(ate);
    }

    private EventoCalendario evento(
            TipoEventoCalendario tipo,
            String titulo,
            String detalhe,
            LocalDate data,
            String hora,
            String status,
            UUID idReferencia,
            Pet pet
    ) {
        return new EventoCalendario(tipo, titulo, detalhe, data, hora, status, idReferencia, pet.getId(), pet.getNome());
    }

    // "1 comprimido · A cada 12 horas"
    private String detalheDoRemedio(Remedio remedio) {

        String detalhe = Stream.of(remedio.getDosagem(), remedio.getFrequencia())
                .filter(parte -> parte != null && !parte.isBlank())
                .collect(Collectors.joining(" · "));

        return detalhe.isEmpty() ? "Remédio" : detalhe;
    }

    // Onde/com quem: clínica do agendamento > veterinário informado > tipo do evento
    private String detalheDaConsulta(Historico consulta) {

        if (consulta.getClinica() != null) {
            return consulta.getClinica().getNome();
        }

        if (consulta.getNomeVeterinario() != null && !consulta.getNomeVeterinario().isBlank()) {
            return consulta.getNomeVeterinario();
        }

        if (consulta.getTipoEvento() == null) {
            return "Consulta";
        }

        return switch (consulta.getTipoEvento()) {
            case CONSULTA -> "Consulta";
            case RETORNO -> "Retorno";
            case CIRURGIA -> "Cirurgia";
            case EXAME -> "Exame";
            case EMERGENCIA -> "Emergência";
            case OUTRO -> "Outro";
        };
    }
}
