package br.com.fiap.java_afetto.model;

import br.com.fiap.java_afetto.model.pet.Pet;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "TBL_REMEDIO")
public class Remedio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_remedio")
    private UUID id;
    @Column(name = "nome_remedio", nullable = false, length = 100)
    private String nomeRemedio;
    @Column(name = "dosagem", length = 100)
    private String dosagem;
    @Column(name = "frequencia", length = 100)
    private String frequencia;
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;
    @Column(name = "data_fim")
    private LocalDate dataFim;
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;


    public Remedio() {}

    public Remedio(UUID id, Pet pet, String nomeRemedio, String dosagem, String frequencia, LocalDate dataInicio, LocalDate dataFim, String observacoes) {
        this.id = id;
        this.pet = pet;
        this.nomeRemedio = nomeRemedio;
        this.dosagem = dosagem;
        this.frequencia = frequencia;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.observacoes = observacoes;
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public Pet getPet() { return pet; }

    public void setPet(Pet pet) { this.pet = pet; }

    public String getNomeRemedio() { return nomeRemedio; }

    public void setNomeRemedio(String nomeRemedio) { this.nomeRemedio = nomeRemedio; }

    public String getDosagem() { return dosagem; }

    public void setDosagem(String dosagem) { this.dosagem = dosagem; }

    public String getFrequencia() { return frequencia; }

    public void setFrequencia(String frequencia) { this.frequencia = frequencia; }

    public LocalDate getDataInicio() { return dataInicio; }

    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }

    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public String getObservacoes() { return observacoes; }

    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
