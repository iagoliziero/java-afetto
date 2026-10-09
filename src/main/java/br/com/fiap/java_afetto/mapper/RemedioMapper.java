package br.com.fiap.java_afetto.mapper;

import br.com.fiap.java_afetto.controller.PetController;
import br.com.fiap.java_afetto.controller.RemedioController;
import br.com.fiap.java_afetto.dto.remedio.RemedioLista;
import br.com.fiap.java_afetto.dto.remedio.RemedioResponse;
import br.com.fiap.java_afetto.model.Remedio;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RemedioMapper {

    public RemedioResponse remedioToResponse(Remedio remedio) {

        Link linkRemedios =
                linkTo(
                        methodOn(RemedioController.class)
                                .readRemedios(0, 50, null)
                )
                        .withRel("Lista de remédios");

        Link linkPet =
                linkTo(
                        methodOn(PetController.class)
                                .readPet(remedio.getPet().getId())
                )
                        .withRel("Detalhes do pet");

        return new RemedioResponse(
                remedio.getId(),
                remedio.getNomeRemedio(),
                remedio.getDosagem(),
                remedio.getFrequencia(),
                remedio.getDataInicio(),
                remedio.getDataFim(),
                remedio.getObservacoes(),
                remedio.getPet().getId(),
                linkRemedios,
                linkPet
        );
    }

    public RemedioLista remedioToResponseLista(Remedio remedio) {

        Link linkRemedio =
                linkTo(
                        methodOn(RemedioController.class)
                                .readRemedio(remedio.getId())
                )
                        .withRel("Detalhes do remédio");

        Link linkPet =
                linkTo(
                        methodOn(PetController.class)
                                .readPet(remedio.getPet().getId())
                )
                        .withRel("Detalhes do pet");

        return new RemedioLista(
                remedio.getId(),
                remedio.getNomeRemedio(),
                remedio.getDosagem(),
                remedio.getFrequencia(),
                remedio.getDataInicio(),
                remedio.getDataFim(),
                remedio.getPet().getId(),
                linkRemedio,
                linkPet
        );
    }
}
