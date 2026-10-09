package br.com.fiap.java_afetto.dto.calendario;

import java.time.LocalDate;
import java.util.List;

public record CalendarioResponse(

        LocalDate inicio,
        LocalDate fim,
        int totalEventos,

        // Ordenados por data e hora
        List<EventoCalendario> eventos

) {
}
