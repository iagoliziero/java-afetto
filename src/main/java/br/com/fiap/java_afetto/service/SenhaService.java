package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.senha.RedefinirSenhaRequest;
import br.com.fiap.java_afetto.exception.MuitasTentativasException;
import br.com.fiap.java_afetto.exception.RegraNegocioException;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.repository.UsuarioRepository;
import br.com.fiap.java_afetto.security.TentativasRedefinicaoSenha;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * "Esqueci a senha" sem e-mail: o tutor confirma e-mail, CPF e data de
 * nascimento do cadastro e define a senha nova.
 *
 * Quando algo não confere, a resposta é sempre a mesma (não diz se o e-mail
 * existe nem qual dado está errado), para ninguém usar isto para descobrir
 * quem tem conta. Errar 5 vezes seguidas bloqueia o e-mail por 15 minutos.
 */
@Service
public class SenhaService {

    private static final String DADOS_NAO_CONFEREM =
            "Os dados não conferem. Confira e-mail, CPF e data de nascimento";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TentativasRedefinicaoSenha tentativas;

    public SenhaService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            TentativasRedefinicaoSenha tentativas
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tentativas = tentativas;
    }

    public void redefinir(RedefinirSenhaRequest request) {

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (tentativas.bloqueado(email)) {
            throw new MuitasTentativasException(
                    "Muitas tentativas. Tente de novo em 15 minutos"
            );
        }

        Usuario usuario = usuarioRepository
                .findFirstByEmailIgnoreCase(email)
                .filter(encontrado -> soDigitos(encontrado.getCpf()).equals(soDigitos(request.cpf())))
                .filter(encontrado -> request.dataNascimento().equals(encontrado.getDataNascimento()))
                .orElse(null);

        if (usuario == null) {
            tentativas.registrarFalha(email);
            throw new RegraNegocioException(DADOS_NAO_CONFEREM);
        }

        tentativas.limpar(email);

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));

        usuarioRepository.save(usuario);
    }

    private String soDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }
}
