package br.com.fiap.java_afetto.model.clinica;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

// Horário de funcionamento da clínica num dia da semana
@Entity
@Table(name = "TBL_EXPEDIENTE")
public class Expediente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_expediente")
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DayOfWeek diaSemana;
    @Column(name = "hora_abertura", nullable = false)
    private LocalTime horaAbertura;
    @Column(name = "hora_fechamento", nullable = false)
    private LocalTime horaFechamento;

    @ManyToOne
    @JoinColumn(name = "id_clinica", nullable = false)
    private Clinica clinica;

    public Expediente() {}

    public Expediente(UUID id, Clinica clinica, DayOfWeek diaSemana, LocalTime horaAbertura, LocalTime horaFechamento) {
        this.id = id;
        this.clinica = clinica;
        this.diaSemana = diaSemana;
        this.horaAbertura = horaAbertura;
        this.horaFechamento = horaFechamento;
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public Clinica getClinica() { return clinica; }

    public void setClinica(Clinica clinica) { this.clinica = clinica; }

    public DayOfWeek getDiaSemana() { return diaSemana; }

    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraAbertura() { return horaAbertura; }

    public void setHoraAbertura(LocalTime horaAbertura) { this.horaAbertura = horaAbertura; }

    public LocalTime getHoraFechamento() { return horaFechamento; }

    public void setHoraFechamento(LocalTime horaFechamento) { this.horaFechamento = horaFechamento; }
}
