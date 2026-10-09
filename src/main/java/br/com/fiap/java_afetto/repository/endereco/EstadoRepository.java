package br.com.fiap.java_afetto.repository.endereco;

import br.com.fiap.java_afetto.model.endereco.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EstadoRepository extends JpaRepository<Estado, UUID> {
    boolean existsByPais_Id(UUID idPais);

    Optional<Estado> findFirstBySiglaIgnoreCaseAndPais_Id(String sigla, UUID idPais);
}
