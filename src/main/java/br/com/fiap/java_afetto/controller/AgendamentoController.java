package br.com.fiap.java_afetto.controller;

import br.com.fiap.java_afetto.dto.agendamento.AgendamentoRequest;
import br.com.fiap.java_afetto.dto.agendamento.HorariosClinicaResponse;
import br.com.fiap.java_afetto.dto.consulta.ConsultaResponse;
import br.com.fiap.java_afetto.service.ClinicaService;
import br.com.fiap.java_afetto.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/clinica/{idClinica}")
@Tag(name = "AGENDAMENTO")
public class AgendamentoController {

    private final ClinicaService clinicaService;
    private final ConsultaService consultaService;

    public AgendamentoController(ClinicaService clinicaService, ConsultaService consultaService) {
        this.clinicaService = clinicaService;
        this.consultaService = consultaService;
    }

    @GetMapping("/horarios")
    @Operation(summary = "Horários livres para agendar (?inicio=AAAA-MM-DD, padrão hoje; ?dias=, padrão 7, máximo 31)")
    public ResponseEntity<HorariosClinicaResponse> readHorarios(
            @PathVariable UUID idClinica,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(defaultValue = "7") int dias
    ) {

        return ResponseEntity.ok(
                clinicaService.horarios(idClinica, inicio, dias)
        );
    }

    @PostMapping("/agendamento")
    @Operation(summary = "Agenda uma consulta na clínica para um pet do usuário logado (vai para o histórico do pet)")
    public ResponseEntity<ConsultaResponse> agendarConsulta(
            @PathVariable UUID idClinica,
            @Valid @RequestBody AgendamentoRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(consultaService.agendarNaClinica(idClinica, request));
    }
}
