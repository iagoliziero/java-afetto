package br.com.fiap.java_afetto.repository.endereco;

import br.com.fiap.java_afetto.model.endereco.Bairro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BairroRepository extends JpaRepository<Bairro, UUID> {
    boolean existsByCidade_Id(UUID idCidade);

    Optional<Bairro> findFirstByNomeIgnoreCaseAndCidade_Id(String nome, UUID idCidade);
}
