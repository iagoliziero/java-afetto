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

        Link linkClinica

) {
}
