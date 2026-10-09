package br.com.fiap.java_afetto.dto.clinica;

import org.springframework.hateoas.Link;

import java.util.UUID;

public record ClinicaLista(

        UUID id,
        String nome,
        String imagemUrl,
        EnderecoClinica endereco,

        // Em km; null quando não há localização para comparar
        Double distanciaKm,
        boolean perto,

        // Favorita do usuário logado
        boolean favorita,

        // Média das notas (1 a 5); null quando ainda não há avaliações
        Double notaMedia,
        long totalAvaliacoes,

        Link linkClinica

) {
}
