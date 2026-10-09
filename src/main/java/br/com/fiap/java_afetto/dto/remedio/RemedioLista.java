package br.com.fiap.java_afetto.dto.remedio;

import org.springframework.hateoas.Link;

import java.time.LocalDate;
import java.util.UUID;

public record RemedioLista(

        UUID id,
        String nomeRemedio,
        String dosagem,
        String frequencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        UUID idPet,

        Link linkRemedio,
        Link linkPet

) {
}
