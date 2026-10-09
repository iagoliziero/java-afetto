package br.com.fiap.java_afetto.dto.agendamento;

import org.springframework.hateoas.Link;

import java.util.List;
import java.util.UUID;

public record HorariosClinicaResponse(

        UUID idClinica,
        String nomeClinica,
        int duracaoMinutos,
        List<DiaDisponivel> dias,

        Link linkClinica

) {
}
