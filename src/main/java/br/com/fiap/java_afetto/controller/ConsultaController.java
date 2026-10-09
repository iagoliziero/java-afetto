package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.consulta.ConsultaLista;
import br.com.fiap.java_afetto.dto.consulta.ConsultaRequest;
import br.com.fiap.java_afetto.dto.consulta.ConsultaResponse;
import br.com.fiap.java_afetto.service.ConsultaService;
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
@RequestMapping("/consulta")
@Tag(name = "CRUD-CONSULTAS")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    @Operation(summary = "Registra uma consulta (futura = agendada, passada = realizada) para um pet do usuário logado")
    public ResponseEntity<ConsultaResponse> createConsulta(
            @Valid @RequestBody ConsultaRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(consultaService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Exibe a consulta")
    public ResponseEntity<ConsultaResponse> readConsulta(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                consultaService.read(id)
        );
    }

    @GetMapping
    @Operation(summary = "Lista as consultas dos pets do usuário logado (?idPet= filtra por um pet)")
    public ResponseEntity<Page<ConsultaLista>> readConsultas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) UUID idPet
    ) {

        // Mais recentes primeiro
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "data", "hora"));

        if (idPet != null) {
            return ResponseEntity.ok(
                    consultaService.readPorPet(idPet, pageable)
            );
        }

        return ResponseEntity.ok(
                consultaService.read(pageable)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza (ou remarca) a consulta")
    public ResponseEntity<ConsultaResponse> updateConsulta(
            @PathVariable UUID id,
            @Valid @RequestBody ConsultaRequest request
    ) {

        return ResponseEntity.ok(
                consultaService.update(id, request)
        );
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela uma consulta agendada")
    public ResponseEntity<ConsultaResponse> cancelarConsulta(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                consultaService.cancelar(id)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta a consulta")
    public ResponseEntity<Void> deleteConsulta(
            @PathVariable UUID id
    ) {

        consultaService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
