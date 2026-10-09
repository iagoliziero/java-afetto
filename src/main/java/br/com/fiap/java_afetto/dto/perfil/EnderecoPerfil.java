package br.com.fiap.java_afetto.dto.perfil;

import java.math.BigDecimal;

public record EnderecoPerfil(

        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,

        // Vêm do CEP; null quando o serviço de CEP não respondeu
        BigDecimal latitude,
        BigDecimal longitude

) {
}
