package br.com.fiap.java_afetto.mapper;

import br.com.fiap.java_afetto.controller.ConsultaController;
import br.com.fiap.java_afetto.controller.PetController;
import br.com.fiap.java_afetto.dto.consulta.ConsultaLista;
import br.com.fiap.java_afetto.dto.consulta.ConsultaResponse;
import br.com.fiap.java_afetto.model.Historico;
import br.com.fiap.java_afetto.model.StatusHistorico;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A consulta é um registro do histórico (TBL_HISTORICO). O status chega
 * calculado pelo ConsultaService (agendada ou realizada depende da data).
 */
@Component
public class ConsultaMapper {

    public ConsultaResponse consultaToResponse(Historico historico, StatusHistorico status) {

        Link linkConsultas =
                linkTo(
                        methodOn(ConsultaController.class)
                                .readConsultas(0, 50, null)
                )
                        .withRel("Lista de consultas");

        Link linkPet =
                linkTo(
                        methodOn(PetController.class)
                                .readPet(historico.getPet().getId())
                )
                        .withRel("Detalhes do pet");

        return new ConsultaResponse(
                historico.getId(),
                historico.getTipoEvento(),
                historico.getDescricao(),
                historico.getData(),
                historico.getHora(),
                historico.getNomeVeterinario(),
                historico.getObservacoes(),
                status,
                historico.getPet().getId(),
                linkConsultas,
                linkPet
        );
    }

    public ConsultaLista consultaToResponseLista(Historico historico, StatusHistorico status) {

        Link linkConsulta =
                linkTo(
                        methodOn(ConsultaController.class)
                                .readConsulta(historico.getId())
                )
                        .withRel("Detalhes da consulta");

        Link linkPet =
                linkTo(
                        methodOn(PetController.class)
                                .readPet(historico.getPet().getId())
                )
                        .withRel("Detalhes do pet");

        return new ConsultaLista(
                historico.getId(),
                historico.getTipoEvento(),
                historico.getDescricao(),
                historico.getData(),
                historico.getHora(),
                status,
                historico.getPet().getId(),
                linkConsulta,
                linkPet
        );
    }
}
