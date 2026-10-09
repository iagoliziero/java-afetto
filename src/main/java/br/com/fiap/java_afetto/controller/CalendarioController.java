package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.calendario.CalendarioResponse;
import br.com.fiap.java_afetto.service.CalendarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/calendario")
@Tag(name = "CALENDARIO")
public class CalendarioController {

    private final CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @GetMapping
    @Operation(summary = "Vacinas, remédios e consultas dos pets do usuário logado no período (?inicio=&fim= AAAA-MM-DD; padrão: mês atual; ?idPet= filtra um pet)")
    public ResponseEntity<CalendarioResponse> readCalendario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID idPet
    ) {

        return ResponseEntity.ok(
                calendarioService.read(inicio, fim, idPet)
        );
    }
}
