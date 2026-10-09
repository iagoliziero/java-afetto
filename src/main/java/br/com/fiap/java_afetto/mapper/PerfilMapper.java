package br.com.fiap.java_afetto.mapper;

import br.com.fiap.java_afetto.controller.UsuarioController;
import br.com.fiap.java_afetto.dto.perfil.EnderecoPerfil;
import br.com.fiap.java_afetto.dto.perfil.PerfilResponse;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.endereco.Endereco;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PerfilMapper {

    public PerfilResponse perfilToResponse(Usuario usuario, long petsCadastrados) {

        Link linkUsuario =
                linkTo(
                        methodOn(UsuarioController.class)
                                .readUsuarioLogado()
                )
                        .withRel("Dados do usuário");

        Integer informada = usuario.getQuantidadePets();
        int quantidade = (int) Math.max(informada != null ? informada : 0, petsCadastrados);

        boolean completo = usuario.getEndereco() != null
                && usuario.getTipoMoradia() != null
                && usuario.getTelaProtecao() != null;

        return new PerfilResponse(
                completo,
                usuario.getTipoMoradia(),
                usuario.getTelaProtecao(),
                quantidade,
                informada,
                petsCadastrados,
                usuario.getEndereco() != null ? enderecoDoPerfil(usuario.getEndereco()) : null,
                linkUsuario
        );
    }

    // Achata a hierarquia Logradouro > Bairro > Cidade > Estado de volta nos campos do formulário
    private EnderecoPerfil enderecoDoPerfil(Endereco endereco) {

        return new EnderecoPerfil(
                endereco.getCep(),
                endereco.getLogradouro().getNome(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getLogradouro().getBairro().getNome(),
                endereco.getLogradouro().getBairro().getCidade().getNome(),
                endereco.getLogradouro().getBairro().getCidade().getEstado().getSigla(),
                endereco.getLatitude(),
                endereco.getLongitude()
        );
    }
}
