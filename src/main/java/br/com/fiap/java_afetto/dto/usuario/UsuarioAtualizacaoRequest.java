package br.com.fiap.java_afetto.dto.usuario;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/*
 * Edição dos dados do usuário (PUT /usuario/{id}). Não tem senha: antes, este
 * PUT gravava como senha o que viesse no corpo, sem conferir a atual. Agora a
 * senha só muda em PUT /usuario/me/senha (com a senha atual) ou no "esqueci a
 * senha". Quem ainda mandar "senha" aqui (versão antiga do app, Postman) não
 * recebe erro: o campo é ignorado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioAtualizacaoRequest(

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória")
        LocalDate dataNascimento,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        String telefone,

        UUID idEndereco

) {
}
