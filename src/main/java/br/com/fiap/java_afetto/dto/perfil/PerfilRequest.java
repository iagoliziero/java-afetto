package br.com.fiap.java_afetto.dto.perfil;

import br.com.fiap.java_afetto.model.TipoMoradia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Os mesmos campos do formulário "Finalize seu cadastro" do app
public record PerfilRequest(

        @NotNull(message = "Selecione o tipo de moradia")
        TipoMoradia tipoMoradia,

        @NotNull(message = "Informe se a casa tem tela de proteção")
        Boolean telaProtecao,

        @NotNull(message = "Informe a quantidade de pets")
        @Min(value = 0, message = "A quantidade de pets não pode ser negativa")
        @Max(value = 50, message = "A quantidade de pets deve ser no máximo 50")
        Integer quantidadePets,

        @NotNull(message = "O endereço é obrigatório")
        @Valid
        EnderecoPerfilRequest endereco

) {
}
