package br.com.fiap.java_afetto.dto.avaliacao;

import org.springframework.hateoas.Link;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvaliacaoResponse(

        UUID id,
        Integer nota,
        String comentario,

        // Só o primeiro nome e a inicial do sobrenome (ex.: "Ana S.")
        String autor,

        // Data da última alteração
        LocalDateTime data,

        // É a avaliação do usuário logado
        boolean minha,

        Link linkClinica

) {
}
