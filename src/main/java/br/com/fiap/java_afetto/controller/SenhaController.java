package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.senha.RedefinirSenhaRequest;
import br.com.fiap.java_afetto.service.SenhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/senha")
@Tag(name = "SENHA")
public class SenhaController {

    private final SenhaService senhaService;

    public SenhaController(SenhaService senhaService) {
        this.senhaService = senhaService;
    }

    @PostMapping("/redefinir")
    @Operation(summary = "Esqueci a senha: confirma e-mail, CPF e data de nascimento e define a senha nova (não precisa estar logado)")
    public ResponseEntity<Void> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest request
    ) {

        senhaService.redefinir(request);

        return ResponseEntity.noContent().build();
    }
}
