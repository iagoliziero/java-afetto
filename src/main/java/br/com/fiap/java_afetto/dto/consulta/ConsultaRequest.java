package br.com.fiap.java_afetto.dto.consulta;

import br.com.fiap.java_afetto.model.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ConsultaRequest(

        // Opcional: sem ele, a consulta é registrada como CONSULTA
        TipoEvento tipoEvento,

        @NotBlank(message = "O motivo da consulta é obrigatório")
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "A data da consulta é obrigatória")
        LocalDate data,

        LocalTime hora,

        @Size(max = 100, message = "O nome do veterinário deve ter no máximo 100 caracteres")
        String nomeVeterinario,

        @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres")
        String observacoes,

        @NotNull(message = "O pet é obrigatório")
        UUID idPet

) {
}
