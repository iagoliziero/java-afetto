package br.com.fiap.java_afetto.dto.senha;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Troca de senha de quem está logado: só vale com a senha atual certa
public record AlterarSenhaRequest(

        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, max = 72, message = "A nova senha deve ter pelo menos 6 caracteres")
        String novaSenha

) {
}
