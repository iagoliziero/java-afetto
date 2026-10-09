package br.com.fiap.java_afetto.dto.senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Sem e-mail de recuperação: o tutor prova quem é com dados do cadastro
public record RedefinirSenhaRequest(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        // Com ou sem pontuação (123.456.789-09 ou 12345678909)
        @NotBlank(message = "O CPF é obrigatório")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória")
        LocalDate dataNascimento,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, max = 72, message = "A nova senha deve ter pelo menos 6 caracteres")
        String novaSenha

) {
}
