package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.agendamento.AgendamentoRequest;
import br.com.fiap.java_afetto.dto.consulta.ConsultaLista;
import br.com.fiap.java_afetto.dto.consulta.ConsultaRequest;
import br.com.fiap.java_afetto.dto.consulta.ConsultaResponse;
import br.com.fiap.java_afetto.exception.RegraNegocioException;
import br.com.fiap.java_afetto.mapper.ConsultaMapper;
import br.com.fiap.java_afetto.model.Historico;
import br.com.fiap.java_afetto.model.StatusHistorico;
import br.com.fiap.java_afetto.model.TipoEvento;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.Vacina;
import br.com.fiap.java_afetto.model.clinica.Clinica;
import br.com.fiap.java_afetto.model.pet.Pet;
import br.com.fiap.java_afetto.repository.ClinicaRepository;
import br.com.fiap.java_afetto.repository.HistoricoRepository;
import br.com.fiap.java_afetto.repository.VacinaRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Consultas do pet. Cada consulta é um registro do histórico (TBL_HISTORICO).
 *
 * Status: data/hora no futuro = AGENDADO; no passado = CONCLUIDO;
 * CANCELADO só pelo PATCH /consulta/{id}/cancelar. Como "agendada" vira
 * "realizada" com o passar do tempo, o status devolvido é sempre recalculado.
 *
 * Consulta agendada numa clínica parceira (POST /clinica/{id}/agendamento)
 * guarda a clínica e só aceita horários livres da agenda dela.
 */
@Service
public class ConsultaService {

    private final HistoricoRepository historicoRepository;
    private final VacinaRepository vacinaRepository;
    private final ConsultaMapper consultaMapper;
    private final UsuarioLogadoService usuarioLogadoService;
    private final ClinicaRepository clinicaRepository;
    private final AgendaService agendaService;
    private final Clock relogio;

    public ConsultaService(
            HistoricoRepository historicoRepository,
            VacinaRepository vacinaRepository,
            ConsultaMapper consultaMapper,
            UsuarioLogadoService usuarioLogadoService,
            ClinicaRepository clinicaRepository,
            AgendaService agendaService,
            Clock relogio
    ) {
        this.historicoRepository = historicoRepository;
        this.vacinaRepository = vacinaRepository;
        this.consultaMapper = consultaMapper;
        this.usuarioLogadoService = usuarioLogadoService;
        this.clinicaRepository = clinicaRepository;
        this.agendaService = agendaService;
        this.relogio = relogio;
    }

    public ConsultaResponse create(ConsultaRequest request) {

        Pet pet = usuarioLogadoService.buscarPetDoUsuario(request.idPet());

        Historico historico = new Historico();

        preencher(historico, request, pet);
        historico.setStatus(statusPelaData(historico));

        return paraResponse(historicoRepository.save(historico));
    }

    // Agendamento numa clínica parceira: nasce AGENDADA e vai para o histórico do pet
    public ConsultaResponse agendarNaClinica(UUID idClinica, AgendamentoRequest request) {

        Pet pet = usuarioLogadoService.buscarPetDoUsuario(request.idPet());

        Clinica clinica = clinicaRepository
                .findById(idClinica)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Clínica não encontrada"
                        )
                );

        LocalTime hora = semSegundos(request.hora());

        agendaService.validarHorario(clinica, request.data(), hora, null);

        Historico historico = new Historico();

        historico.setTipoEvento(TipoEvento.CONSULTA);
        historico.setDescricao(
                request.descricao() == null || request.descricao().isBlank()
                        ? "Consulta na " + clinica.getNome()
                        : request.descricao().trim()
        );
        historico.setData(request.data());
        historico.setHora(hora);
        historico.setObservacoes(request.observacoes());
        historico.setPet(pet);
        historico.setClinica(clinica);
        historico.setStatus(StatusHistorico.AGENDADO);

        return paraResponse(historicoRepository.save(historico));
    }

    public ConsultaResponse read(UUID id) {

        return paraResponse(buscarConsultaDoUsuario(id));
    }

    // Só as consultas dos pets do usuário logado
    public Page<ConsultaLista> read(Pageable pageable) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        return historicoRepository
                .findByPet_Usuario_Id(usuario.getId(), pageable)
                .map(this::paraLista);
    }

    public Page<ConsultaLista> readPorPet(UUID idPet, Pageable pageable) {

        Pet pet = usuarioLogadoService.buscarPetDoUsuario(idPet);

        return historicoRepository
                .findByPet_Id(pet.getId(), pageable)
                .map(this::paraLista);
    }

    public ConsultaResponse update(
            UUID id,
            ConsultaRequest request
    ) {

        Historico historico = buscarConsultaDoUsuario(id);

        if (historico.getStatus() == StatusHistorico.CANCELADO) {
            throw new RegraNegocioException(
                    "Consulta cancelada não pode ser alterada"
            );
        }

        Pet pet = usuarioLogadoService.buscarPetDoUsuario(request.idPet());

        // Remarcar um agendamento de clínica: o novo horário precisa estar livre na agenda dela
        if (historico.getClinica() != null) {
            LocalTime novaHora = semSegundos(request.hora());
            boolean mudouHorario = !Objects.equals(request.data(), historico.getData())
                    || !Objects.equals(novaHora, historico.getHora());

            if (mudouHorario) {
                agendaService.validarHorario(historico.getClinica(), request.data(), novaHora, historico.getId());
            }
        }

        preencher(historico, request, pet);
        historico.setStatus(statusPelaData(historico));

        return paraResponse(historicoRepository.save(historico));
    }

    public ConsultaResponse cancelar(UUID id) {

        Historico historico = buscarConsultaDoUsuario(id);

        if (historico.getStatus() == StatusHistorico.CANCELADO) {
            throw new RegraNegocioException(
                    "A consulta já está cancelada"
            );
        }

        if (statusAtual(historico) != StatusHistorico.AGENDADO) {
            throw new RegraNegocioException(
                    "Só é possível cancelar uma consulta agendada"
            );
        }

        historico.setStatus(StatusHistorico.CANCELADO);

        return paraResponse(historicoRepository.save(historico));
    }

    @Transactional
    public void delete(UUID id) {

        Historico historico = buscarConsultaDoUsuario(id);

        // Vacinas aplicadas nesta consulta continuam no pet, só perdem o vínculo
        for (Vacina vacina : vacinaRepository.findByHistorico_Id(historico.getId())) {
            vacina.setHistorico(null);
        }

        historicoRepository.delete(historico);
    }

    // Consulta de pet de outro usuário responde como "não encontrada"
    private Historico buscarConsultaDoUsuario(UUID id) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        return historicoRepository
                .findByIdAndPet_Usuario_Id(id, usuario.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Consulta não encontrada"
                        )
                );
    }

    private void preencher(Historico historico, ConsultaRequest request, Pet pet) {

        historico.setTipoEvento(
                request.tipoEvento() != null ? request.tipoEvento() : TipoEvento.CONSULTA
        );
        historico.setDescricao(request.descricao());
        historico.setData(request.data());
        historico.setHora(semSegundos(request.hora()));
        historico.setNomeVeterinario(request.nomeVeterinario());
        historico.setObservacoes(request.observacoes());
        historico.setPet(pet);
    }

    // 09:30:45 vira 09:30 (os horários da agenda são de 30 em 30 minutos)
    private LocalTime semSegundos(LocalTime hora) {
        return hora != null ? hora.truncatedTo(ChronoUnit.MINUTES) : null;
    }

    // Sem horário, a consulta conta como agendada até o fim do dia
    private StatusHistorico statusPelaData(Historico historico) {

        LocalDateTime agora = LocalDateTime.now(relogio);

        boolean noFuturo = historico.getHora() != null
                ? LocalDateTime.of(historico.getData(), historico.getHora()).isAfter(agora)
                : !historico.getData().isBefore(agora.toLocalDate());

        return noFuturo ? StatusHistorico.AGENDADO : StatusHistorico.CONCLUIDO;
    }

    // Status que vale agora (o calendário também usa)
    public StatusHistorico statusAtual(Historico historico) {

        if (historico.getStatus() == StatusHistorico.CANCELADO || historico.getData() == null) {
            return historico.getStatus();
        }

        return statusPelaData(historico);
    }

    private ConsultaResponse paraResponse(Historico historico) {
        return consultaMapper.consultaToResponse(historico, statusAtual(historico));
    }

    private ConsultaLista paraLista(Historico historico) {
        return consultaMapper.consultaToResponseLista(historico, statusAtual(historico));
    }
}
