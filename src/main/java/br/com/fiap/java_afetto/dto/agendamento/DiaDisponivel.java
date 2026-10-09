package br.com.fiap.java_afetto.dto.agendamento;

import java.time.LocalDate;
import java.util.List;

public record DiaDisponivel(

        LocalDate data,

        // Ex.: "segunda-feira"
        String diaSemana,

        // A clínica abre nesse dia da semana
        boolean aberta,

        // Horários livres no formato "HH:mm" (sem os que já passaram ou já foram agendados)
        List<String> horarios

) {
}
