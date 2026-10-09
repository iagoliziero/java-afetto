package br.com.fiap.java_afetto.dto.agendamento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AgendamentoRequest(

        @NotNull(message = "O pet é obrigatório")
        UUID idPet,

        @NotNull(message = "O dia é obrigatório")
        LocalDate data,

        // Um dos horários livres devolvidos por GET /clinica/{id}/horarios (ex.: "09:30")
        @NotNull(message = "O horário é obrigatório")
        LocalTime hora,

        // Opcional: sem ele, fica "Consulta na <nome da clínica>"
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String descricao,

        @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres")
        String observacoes

) {
}
