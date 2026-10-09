package br.com.fiap.java_afetto.dto.remedio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record RemedioRequest(

        @NotBlank(message = "O nome do remédio é obrigatório")
        @Size(max = 100, message = "O nome do remédio deve ter no máximo 100 caracteres")
        String nomeRemedio,

        @Size(max = 100, message = "A dosagem deve ter no máximo 100 caracteres")
        String dosagem,

        @Size(max = 100, message = "A frequência deve ter no máximo 100 caracteres")
        String frequencia,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate dataInicio,

        LocalDate dataFim,

        String observacoes,

        @NotNull(message = "O pet é obrigatório")
        UUID idPet

) {
}
