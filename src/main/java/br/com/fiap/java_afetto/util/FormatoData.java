package br.com.fiap.java_afetto.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

// Como horários e dias da semana aparecem para o app
public final class FormatoData {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale PORTUGUES = Locale.of("pt", "BR");

    private FormatoData() {
    }

    // 09:30
    public static String hora(LocalTime hora) {
        return hora.format(HORA);
    }

    // segunda-feira
    public static String diaDaSemana(DayOfWeek dia) {
        return dia.getDisplayName(TextStyle.FULL, PORTUGUES);
    }
}
