package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.clinica.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    Page<Avaliacao> findByClinica_Id(UUID idClinica, Pageable pageable);

    Optional<Avaliacao> findByUsuario_IdAndClinica_Id(UUID idUsuario, UUID idClinica);

    // Média e quantidade de avaliações de todas as clínicas, numa consulta só
    @Query("select a.clinica.id as idClinica, avg(a.nota) as notaMedia, count(a) as quantidade "
            + "from Avaliacao a group by a.clinica.id")
    List<ResumoAvaliacao> resumirPorClinica();

    @Query("select a.clinica.id as idClinica, avg(a.nota) as notaMedia, count(a) as quantidade "
            + "from Avaliacao a where a.clinica.id = :idClinica group by a.clinica.id")
    Optional<ResumoAvaliacao> resumirDaClinica(@Param("idClinica") UUID idClinica);

    interface ResumoAvaliacao {

        UUID getIdClinica();

        Double getNotaMedia();

        Long getQuantidade();
    }
}
