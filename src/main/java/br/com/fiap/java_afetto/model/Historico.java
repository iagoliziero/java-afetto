package br.com.fiap.java_afetto.model;

import br.com.fiap.java_afetto.model.clinica.Clinica;
import br.com.fiap.java_afetto.model.clinica.Veterinario;
import br.com.fiap.java_afetto.model.pet.Pet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "TBL_HISTORICO")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_historico")
    private UUID id;
    @Column(name = "data")
    private LocalDate data;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento")
    private TipoEvento tipoEvento;
    @Column(name = "descricao")
    private String descricao;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusHistorico status;
    @Column(name = "observacoes")
    private String observacoes;
    @Column(name = "hora")
    private LocalTime hora;
    @Column(name = "nome_veterinario", length = 100)
    private String nomeVeterinario;

    @ManyToOne
    @JoinColumn(name = "id_pet")
    private Pet pet;
    @ManyToOne
    @JoinColumn(name = "id_veterinario")
    private Veterinario veterinario;
    // Preenchida quando a consulta foi agendada pelo app numa clínica parceira
    @ManyToOne
    @JoinColumn(name = "id_clinica")
    private Clinica clinica;
    @JsonIgnore
    @OneToMany(mappedBy = "historico")
    private List<Vacina> vacinas;

    public Historico() {}

    public Historico(UUID id, Pet pet, Veterinario veterinario, LocalDate data, TipoEvento tipoEvento, String descricao, StatusHistorico status, String observacoes, List<Vacina> vacinas) {
        this.id = id;
        this.pet = pet;
        this.veterinario = veterinario;
        this.data = data;
        this.tipoEvento = tipoEvento;
        this.descricao = descricao;
        this.status = status;
        this.observacoes = observacoes;
        this.vacinas = vacinas;
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public Pet getPet() { return pet; }

    public void setPet(Pet pet) { this.pet = pet; }

    public Veterinario getVeterinario() { return veterinario; }

    public void setVeterinario(Veterinario veterinario) { this.veterinario = veterinario; }

    public LocalDate getData() { return data; }

    public void setData(LocalDate data) { this.data = data; }

    public TipoEvento getTipoEvento() { return tipoEvento; }

    public void setTipoEvento(TipoEvento tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) { this.descricao = descricao; }

    public StatusHistorico getStatus() { return status; }

    public void setStatus(StatusHistorico status) { this.status = status; }

    public String getObservacoes() { return observacoes; }

    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalTime getHora() { return hora; }

    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getNomeVeterinario() { return nomeVeterinario; }

    public void setNomeVeterinario(String nomeVeterinario) { this.nomeVeterinario = nomeVeterinario; }

    public Clinica getClinica() { return clinica; }

    public void setClinica(Clinica clinica) { this.clinica = clinica; }

    public List<Vacina> getVacinas() { return vacinas; }

    public void setVacinas(List<Vacina> vacinas) { this.vacinas = vacinas; }
}