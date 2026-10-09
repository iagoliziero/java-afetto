package br.com.fiap.java_afetto.dto.clinica;

import br.com.fiap.java_afetto.model.clinica.Turno;

public record VeterinarioClinica(

        String nome,
        String especialidade,
        Turno turno

) {
}
