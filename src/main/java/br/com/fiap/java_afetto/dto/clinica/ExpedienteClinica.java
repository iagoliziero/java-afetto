package br.com.fiap.java_afetto.dto.clinica;

public record ExpedienteClinica(

        // Ex.: "segunda-feira"
        String diaSemana,

        // "HH:mm"
        String abertura,
        String fechamento

) {
}
