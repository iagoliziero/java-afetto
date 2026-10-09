package br.com.fiap.java_afetto.dto.clinica;

import org.springframework.hateoas.Link;

import java.util.List;
import java.util.UUID;

public record ClinicaResponse(

        UUID id,
        String nome,
        String descricao,
        String imagemUrl,
        String telefone,
        String email,
        String site,
        EnderecoClinica endereco,

        // Em km; null quando não há localização para comparar
        Double distanciaKm,
        boolean perto,

        // Favorita do usuário logado
        boolean favorita,

        // Média das notas (1 a 5); null quando ainda não há avaliações
        Double notaMedia,
        long totalAvaliacoes,

        List<VeterinarioClinica> veterinarios,

        // Horário de funcionamento, de segunda a domingo (só os dias em que abre)
        List<ExpedienteClinica> expediente,

        Link linkClinicas

) {
}
