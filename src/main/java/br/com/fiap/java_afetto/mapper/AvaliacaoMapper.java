package br.com.fiap.java_afetto.mapper;

import br.com.fiap.java_afetto.controller.ClinicaController;
import br.com.fiap.java_afetto.dto.avaliacao.AvaliacaoResponse;
import br.com.fiap.java_afetto.model.clinica.Avaliacao;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AvaliacaoMapper {

    public AvaliacaoResponse avaliacaoToResponse(Avaliacao avaliacao, boolean minha) {

        Link linkClinica =
                linkTo(
                        methodOn(ClinicaController.class)
                                .readClinica(avaliacao.getClinica().getId(), null, null)
                )
                        .withRel("Detalhes da clínica");

        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                nomeDoAutor(avaliacao.getUsuario().getNome()),
                avaliacao.getDataAtualizacao() != null
                        ? avaliacao.getDataAtualizacao()
                        : avaliacao.getDataCriacao(),
                minha,
                linkClinica
        );
    }

    // Privacidade: a avaliação é pública, então só o primeiro nome e a inicial do sobrenome
    private String nomeDoAutor(String nomeCompleto) {

        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "Tutor";
        }

        String[] partes = nomeCompleto.trim().split("\\s+");

        if (partes.length == 1) {
            return partes[0];
        }

        return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
    }
}
