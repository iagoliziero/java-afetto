package br.com.fiap.java_afetto.dto.perfil;

import br.com.fiap.java_afetto.model.TipoMoradia;
import org.springframework.hateoas.Link;

public record PerfilResponse(

        // Tem moradia, tela de proteção e endereço preenchidos
        boolean perfilCompleto,

        TipoMoradia tipoMoradia,
        Boolean telaProtecao,

        // Quantidade que vale: o maior entre o que o tutor informou e os pets cadastrados
        int quantidadePets,
        Integer quantidadePetsInformada,
        long petsCadastrados,

        // null enquanto o cadastro não foi finalizado
        EnderecoPerfil endereco,

        Link linkUsuario

) {
}
