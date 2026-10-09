package br.com.fiap.java_afetto.model.clinica;

import br.com.fiap.java_afetto.model.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TBL_CLINICA_FAVORITA")
public class ClinicaFavorita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_favorita")
    private UUID id;
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @ManyToOne
    @JoinColumn(name = "id_clinica", nullable = false)
    private Clinica clinica;

    public ClinicaFavorita() {}

    public ClinicaFavorita(UUID id, Usuario usuario, Clinica clinica, LocalDateTime dataCriacao) {
        this.id = id;
        this.usuario = usuario;
        this.clinica = clinica;
        this.dataCriacao = dataCriacao;
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Clinica getClinica() { return clinica; }

    public void setClinica(Clinica clinica) { this.clinica = clinica; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
