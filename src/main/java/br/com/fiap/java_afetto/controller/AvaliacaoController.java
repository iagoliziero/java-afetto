package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.avaliacao.AvaliacaoRequest;
import br.com.fiap.java_afetto.dto.avaliacao.AvaliacaoResponse;
import br.com.fiap.java_afetto.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clinica/{idClinica}/avaliacao")
@Tag(name = "AVALIACOES")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    @Operation(summary = "Lista as avaliações e comentários da clínica (mais recentes primeiro)")
    public ResponseEntity<Page<AvaliacaoResponse>> readAvaliacoes(
            @PathVariable UUID idClinica,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCriacao"));

        return ResponseEntity.ok(
                avaliacaoService.read(idClinica, pageable)
        );
    }

    @PutMapping
    @Operation(summary = "Avalia a clínica (nota de 1 a 5 e comentário opcional); avaliar de novo atualiza")
    public ResponseEntity<AvaliacaoResponse> avaliarClinica(
            @PathVariable UUID idClinica,
            @Valid @RequestBody AvaliacaoRequest request
    ) {

        return ResponseEntity.ok(
                avaliacaoService.avaliar(idClinica, request)
        );
    }

    @DeleteMapping
    @Operation(summary = "Apaga a avaliação do usuário logado nesta clínica")
    public ResponseEntity<Void> deleteAvaliacao(
            @PathVariable UUID idClinica
    ) {

        avaliacaoService.delete(idClinica);

        return ResponseEntity.noContent().build();
    }
}
