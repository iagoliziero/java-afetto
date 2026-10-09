package br.com.fiap.java_afetto.dto.remedio;

import org.springframework.hateoas.Link;

import java.time.LocalDate;
import java.util.UUID;

public record RemedioResponse(

        UUID id,
        String nomeRemedio,
        String dosagem,
        String frequencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        String observacoes,
        UUID idPet,

        Link linkRemedios,
        Link linkPet

) {
}
