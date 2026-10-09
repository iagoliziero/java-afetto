package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.clinica.ClinicaLista;
import br.com.fiap.java_afetto.dto.clinica.ClinicaResponse;
import br.com.fiap.java_afetto.service.ClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/clinica")
@Tag(name = "CLINICAS")
public class ClinicaController {

    private final ClinicaService clinicaService;

    public ClinicaController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    @GetMapping
    @Operation(summary = "Lista as clínicas, mais perto primeiro (?nome= busca; ?latitude=&longitude= é a localização do app; ?favoritas=true só as favoritas)")
    public ResponseEntity<Page<ClinicaLista>> readClinicas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(defaultValue = "false") boolean favoritas
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                clinicaService.read(pageable, nome, latitude, longitude, favoritas)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Exibe a clínica com contatos, endereço e equipe")
    public ResponseEntity<ClinicaResponse> readClinica(
            @PathVariable UUID id,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude
    ) {

        return ResponseEntity.ok(
                clinicaService.read(id, latitude, longitude)
        );
    }

    @PutMapping("/{id}/favorito")
    @Operation(summary = "Marca a clínica como favorita do usuário logado")
    public ResponseEntity<Void> favoritarClinica(
            @PathVariable UUID id
    ) {

        clinicaService.favoritar(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/favorito")
    @Operation(summary = "Tira a clínica das favoritas do usuário logado")
    public ResponseEntity<Void> desfavoritarClinica(
            @PathVariable UUID id
    ) {

        clinicaService.desfavoritar(id);

        return ResponseEntity.noContent().build();
    }
}
