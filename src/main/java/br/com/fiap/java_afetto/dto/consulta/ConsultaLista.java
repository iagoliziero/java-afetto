package br.com.fiap.java_afetto.dto.consulta;

import br.com.fiap.java_afetto.model.StatusHistorico;
import br.com.fiap.java_afetto.model.TipoEvento;
import org.springframework.hateoas.Link;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ConsultaLista(

        UUID id,
        TipoEvento tipoEvento,
        String descricao,
        LocalDate data,
        LocalTime hora,
        StatusHistorico status,
        UUID idPet,

        // Preenchidos quando a consulta foi agendada numa clínica parceira
        UUID idClinica,
        String nomeClinica,

        Link linkConsulta,
        Link linkPet

) {
}
