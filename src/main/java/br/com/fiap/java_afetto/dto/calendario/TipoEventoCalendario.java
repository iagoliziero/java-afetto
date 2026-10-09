package br.com.fiap.java_afetto.dto.calendario;

public enum TipoEventoCalendario {
    // Dia em que a vacina foi (ou será) aplicada
    VACINA,
    // Dia da próxima dose de uma vacina
    PROXIMA_DOSE,
    // Um evento por dia de tratamento
    REMEDIO,
    // Consultas, retornos, exames... (inclui os agendamentos nas clínicas)
    CONSULTA
}
