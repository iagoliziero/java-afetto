package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.agendamento.DiaDisponivel;
import br.com.fiap.java_afetto.exception.ConflitoException;
import br.com.fiap.java_afetto.exception.RegraNegocioException;
import br.com.fiap.java_afetto.model.Historico;
import br.com.fiap.java_afetto.model.StatusHistorico;
import br.com.fiap.java_afetto.model.clinica.Clinica;
import br.com.fiap.java_afetto.model.clinica.Expediente;
import br.com.fiap.java_afetto.repository.ExpedienteRepository;
import br.com.fiap.java_afetto.repository.HistoricoRepository;
import br.com.fiap.java_afetto.util.FormatoData;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agenda das clínicas: monta os horários livres a partir do horário de
 * funcionamento (TBL_EXPEDIENTE), em blocos de 30 minutos, e confere se um
 * horário pode ser agendado. Horário livre = dentro do expediente, ainda não
 * passou (no fuso do Brasil) e sem outra consulta não cancelada na clínica.
 */
@Service
public class AgendaService {

    public static final int DURACAO_MINUTOS = 30;
    private static final int MAX_DIAS = 31;

    private final ExpedienteRepository expedienteRepository;
    private final HistoricoRepository historicoRepository;
    private final Clock relogio;

    public AgendaService(
            ExpedienteRepository expedienteRepository,
            HistoricoRepository historicoRepository,
            Clock relogio
    ) {
        this.expedienteRepository = expedienteRepository;
        this.historicoRepository = historicoRepository;
        this.relogio = relogio;
    }

    // Os próximos dias (a partir de "inicio", ou de hoje) com os horários livres de cada um
    public List<DiaDisponivel> horariosLivres(Clinica clinica, LocalDate inicio, int dias) {

        LocalDateTime agora = LocalDateTime.now(relogio);
        LocalDate primeiro = inicio == null || inicio.isBefore(agora.toLocalDate())
                ? agora.toLocalDate()
                : inicio;
        LocalDate ultimo = primeiro.plusDays(Math.max(1, Math.min(dias, MAX_DIAS)) - 1);

        Map<DayOfWeek, Expediente> expedientePorDia = expedienteRepository
                .findByClinica_Id(clinica.getId())
                .stream()
                .collect(Collectors.toMap(Expediente::getDiaSemana, expediente -> expediente));

        Set<LocalDateTime> ocupados = historicoRepository
                .findByClinica_IdAndDataBetweenAndStatusNot(clinica.getId(), primeiro, ultimo, StatusHistorico.CANCELADO)
                .stream()
                .filter(consulta -> consulta.getHora() != null)
                .map(consulta -> LocalDateTime.of(consulta.getData(), consulta.getHora()))
                .collect(Collectors.toSet());

        List<DiaDisponivel> resultado = new ArrayList<>();

        for (LocalDate data = primeiro; !data.isAfter(ultimo); data = data.plusDays(1)) {

            Expediente expediente = expedientePorDia.get(data.getDayOfWeek());
            List<String> horarios = new ArrayList<>();

            if (expediente != null) {
                for (LocalTime hora : horariosDoExpediente(expediente)) {
                    LocalDateTime momento = LocalDateTime.of(data, hora);
                    if (momento.isAfter(agora) && !ocupados.contains(momento)) {
                        horarios.add(FormatoData.hora(hora));
                    }
                }
            }

            resultado.add(new DiaDisponivel(
                    data,
                    FormatoData.diaDaSemana(data.getDayOfWeek()),
                    expediente != null,
                    horarios
            ));
        }

        return resultado;
    }

    /**
     * Confere se dá para agendar nesse dia e horário. idConsultaAtual é a
     * consulta sendo remarcada (o horário dela mesma não conta como ocupado).
     */
    public void validarHorario(Clinica clinica, LocalDate data, LocalTime hora, UUID idConsultaAtual) {

        if (data == null || hora == null) {
            throw new RegraNegocioException("Escolha o dia e o horário do agendamento");
        }

        if (!LocalDateTime.of(data, hora).isAfter(LocalDateTime.now(relogio))) {
            throw new RegraNegocioException("Escolha um horário a partir de agora");
        }

        Expediente expediente = expedienteRepository
                .findByClinica_IdAndDiaSemana(clinica.getId(), data.getDayOfWeek())
                .orElseThrow(() -> new RegraNegocioException("A clínica não atende nesse dia"));

        if (!horariosDoExpediente(expediente).contains(hora)) {
            throw new RegraNegocioException("A clínica não atende nesse horário");
        }

        boolean ocupado = historicoRepository
                .findByClinica_IdAndDataAndHoraAndStatusNot(clinica.getId(), data, hora, StatusHistorico.CANCELADO)
                .stream()
                .map(Historico::getId)
                .anyMatch(id -> !Objects.equals(id, idConsultaAtual));

        if (ocupado) {
            throw new ConflitoException("Esse horário já foi agendado. Escolha outro");
        }
    }

    // Blocos de 30 min que cabem inteiros no expediente (conta em minutos para não "virar" a meia-noite)
    private List<LocalTime> horariosDoExpediente(Expediente expediente) {

        List<LocalTime> horarios = new ArrayList<>();
        int abertura = expediente.getHoraAbertura().toSecondOfDay() / 60;
        int fechamento = expediente.getHoraFechamento().toSecondOfDay() / 60;

        for (int minuto = abertura; minuto + DURACAO_MINUTOS <= fechamento; minuto += DURACAO_MINUTOS) {
            horarios.add(LocalTime.of(minuto / 60, minuto % 60));
        }

        return horarios;
    }
}
