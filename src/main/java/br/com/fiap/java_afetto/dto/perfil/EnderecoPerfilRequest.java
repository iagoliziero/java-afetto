package br.com.fiap.java_afetto.dto.perfil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoPerfilRequest(

        @NotBlank(message = "O CEP é obrigatório")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
        String cep,

        @NotBlank(message = "O logradouro é obrigatório")
        @Size(max = 255, message = "O logradouro deve ter no máximo 255 caracteres")
        String logradouro,

        @NotBlank(message = "O número é obrigatório")
        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
        String numero,

        @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres")
        String complemento,

        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 255, message = "O bairro deve ter no máximo 255 caracteres")
        String bairro,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 255, message = "A cidade deve ter no máximo 255 caracteres")
        String cidade,

        // UF com 2 letras (ex.: "SP")
        @NotBlank(message = "O estado é obrigatório")
        @Pattern(regexp = "[A-Za-z]{2}", message = "Informe a UF com 2 letras")
        String estado

) {
}
