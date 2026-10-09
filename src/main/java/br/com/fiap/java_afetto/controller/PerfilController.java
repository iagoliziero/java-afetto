package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.perfil.PerfilRequest;
import br.com.fiap.java_afetto.dto.perfil.PerfilResponse;
import br.com.fiap.java_afetto.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario/me/perfil")
@Tag(name = "PERFIL")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    @Operation(summary = "Dados do \"Finalize seu cadastro\" do usuário logado (e se o perfil está completo)")
    public ResponseEntity<PerfilResponse> readPerfil() {

        return ResponseEntity.ok(
                perfilService.read()
        );
    }

    @PutMapping
    @Operation(summary = "Salva o \"Finalize seu cadastro\": moradia, tela de proteção, quantidade de pets e endereço")
    public ResponseEntity<PerfilResponse> updatePerfil(
            @Valid @RequestBody PerfilRequest request
    ) {

        return ResponseEntity.ok(
                perfilService.update(request)
        );
    }
}
