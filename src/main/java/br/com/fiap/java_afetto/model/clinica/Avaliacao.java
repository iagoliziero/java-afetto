package br.com.fiap.java_afetto.model.clinica;

import br.com.fiap.java_afetto.model.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TBL_AVALIACAO")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_avaliacao")
    private UUID id;
    @Column(name = "nota", nullable = false)
    private Integer nota;
    @Column(name = "comentario", length = 500)
    private String comentario;
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @ManyToOne
    @JoinColumn(name = "id_clinica", nullable = false)
    private Clinica clinica;

    public Avaliacao() {}

    public Avaliacao(UUID id, Usuario usuario, Clinica clinica, Integer nota, String comentario, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.usuario = usuario;
        this.clinica = clinica;
        this.nota = nota;
        this.comentario = comentario;
        this.dataCriacao = dataCriacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Clinica getClinica() { return clinica; }

    public void setClinica(Clinica clinica) { this.clinica = clinica; }

    public Integer getNota() { return nota; }

    public void setNota(Integer nota) { this.nota = nota; }

    public String getComentario() { return comentario; }

    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
