package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.Vacina;
import br.com.fiap.java_afetto.model.pet.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VacinaRepository extends JpaRepository<Vacina, UUID> {

    Page<Vacina> findByPet_Id(UUID idPet, Pageable pageable);

    List<Vacina> findByHistorico_Id(UUID idHistorico);

    List<Vacina> findByPet_Usuario_Id(UUID idUsuario);

    // GET /vacina sem ?idPet=: as vacinas de todos os pets da conta
    Page<Vacina> findByPet_Usuario_Id(UUID idUsuario, Pageable pageable);

    void deleteByPet_Id(UUID idPet);
}
