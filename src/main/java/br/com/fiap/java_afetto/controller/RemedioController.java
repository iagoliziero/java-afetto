package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.remedio.RemedioLista;
import br.com.fiap.java_afetto.dto.remedio.RemedioRequest;
import br.com.fiap.java_afetto.dto.remedio.RemedioResponse;
import br.com.fiap.java_afetto.service.RemedioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/remedio")
@Tag(name = "CRUD-REMEDIOS")
public class RemedioController {

    private final RemedioService remedioService;

    public RemedioController(RemedioService remedioService) {
        this.remedioService = remedioService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um remédio para um pet do usuário logado")
    public ResponseEntity<RemedioResponse> createRemedio(
            @Valid @RequestBody RemedioRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(remedioService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Exibe o remédio")
    public ResponseEntity<RemedioResponse> readRemedio(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                remedioService.read(id)
        );
    }

    @GetMapping
    @Operation(summary = "Lista os remédios dos pets do usuário logado (?idPet= filtra por um pet)")
    public ResponseEntity<Page<RemedioLista>> readRemedios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) UUID idPet
    ) {

        // Mais recentes primeiro
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataInicio").descending());

        if (idPet != null) {
            return ResponseEntity.ok(
                    remedioService.readPorPet(idPet, pageable)
            );
        }

        return ResponseEntity.ok(
                remedioService.read(pageable)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza o remédio")
    public ResponseEntity<RemedioResponse> updateRemedio(
            @PathVariable UUID id,
            @Valid @RequestBody RemedioRequest request
    ) {

        return ResponseEntity.ok(
                remedioService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta o remédio")
    public ResponseEntity<Void> deleteRemedio(
            @PathVariable UUID id
    ) {

        remedioService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
