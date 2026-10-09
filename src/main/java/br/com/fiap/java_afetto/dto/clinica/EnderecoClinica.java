package br.com.fiap.java_afetto.dto.clinica;

import java.math.BigDecimal;

public record EnderecoClinica(

        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep,
        BigDecimal latitude,
        BigDecimal longitude

) {
}
