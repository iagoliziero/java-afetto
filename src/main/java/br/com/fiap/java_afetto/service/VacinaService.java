package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.vacina.VacinaLista;
import br.com.fiap.java_afetto.dto.vacina.VacinaRequest;
import br.com.fiap.java_afetto.dto.vacina.VacinaResponse;
import br.com.fiap.java_afetto.mapper.VacinaMapper;
import br.com.fiap.java_afetto.model.Historico;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.pet.Pet;
import br.com.fiap.java_afetto.model.Vacina ;
import br.com.fiap.java_afetto.repository.HistoricoRepository;
import br.com.fiap.java_afetto.repository.VacinaRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Vacinas só dos pets de quem está logado (o ADMIN vê todas). Vacina ou pet de
 * outra conta responde "não encontrado" (404), igual a um id que não existe.
 */
@Service
public class VacinaService {

    private final VacinaRepository vacinaRepository;
    private final VacinaMapper vacinaMapper;
    private final HistoricoRepository historicoRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    public VacinaService(
            VacinaRepository vacinaRepository,
            VacinaMapper vacinaMapper,
            HistoricoRepository historicoRepository,
            UsuarioLogadoService usuarioLogadoService
    ) {
        this.vacinaRepository = vacinaRepository;
        this.vacinaMapper = vacinaMapper;
        this.historicoRepository = historicoRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public VacinaResponse create(VacinaRequest request) {

        Pet pet = usuarioLogadoService.buscarPetPermitido(request.idPet());

        Vacina vacina = new Vacina();

        vacina.setNomeVacina(request.nomeVacina());
        vacina.setFabricante(request.fabricante());
        vacina.setLote(request.lote());
        vacina.setDataAplicacao(request.dataAplicacao());
        vacina.setProximaDose(request.proximaDose());
        vacina.setObservacoes(request.observacoes());
        vacina.setPet(pet);
        vacina.setHistorico(historicoDoPet(request.idHistorico(), pet));

        return vacinaMapper.vacinaToResponse(
                vacinaRepository.save(vacina)
        );
    }

    public VacinaResponse read(UUID id) {

        return vacinaMapper.vacinaToResponse(
                buscarVacinaPermitida(id)
        );
    }

    // Sem ?idPet=: as vacinas de todos os pets da conta (antes vinham as de todas as contas)
    public Page<VacinaLista> read(Pageable pageable) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        Page<Vacina> vacinas = usuarioLogadoService.ehAdmin(usuario)
                ? vacinaRepository.findAll(pageable)
                : vacinaRepository.findByPet_Usuario_Id(usuario.getId(), pageable);

        return vacinas.map(vacinaMapper::vacinaToResponseLista);
    }

    public Page<VacinaLista> readPorPet(UUID idPet, Pageable pageable) {

        usuarioLogadoService.buscarPetPermitido(idPet);

        return vacinaRepository
                .findByPet_Id(idPet, pageable)
                .map(vacinaMapper::vacinaToResponseLista);
    }

    public VacinaResponse update(
            UUID id,
            VacinaRequest request
    ) {

        Vacina vacina = buscarVacinaPermitida(id);

        Pet pet = usuarioLogadoService.buscarPetPermitido(request.idPet());

        vacina.setNomeVacina(request.nomeVacina());
        vacina.setFabricante(request.fabricante());
        vacina.setLote(request.lote());
        vacina.setDataAplicacao(request.dataAplicacao());
        vacina.setProximaDose(request.proximaDose());
        vacina.setObservacoes(request.observacoes());
        vacina.setPet(pet);
        // Sem idHistorico, a vacina deixa de apontar para uma consulta
        vacina.setHistorico(historicoDoPet(request.idHistorico(), pet));

        return vacinaMapper.vacinaToResponse(
                vacinaRepository.save(vacina)
        );
    }

    public void delete(UUID id) {

        vacinaRepository.delete(buscarVacinaPermitida(id));
    }

    private Vacina buscarVacinaPermitida(UUID id) {

        Vacina vacina = vacinaRepository
                .findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Vacina não encontrada"
                        )
                );

        Usuario logado = usuarioLogadoService.buscarUsuarioLogado();
        Pet pet = vacina.getPet();
        boolean doUsuario = pet != null
                && pet.getUsuario() != null
                && pet.getUsuario().getId().equals(logado.getId());

        if (!doUsuario && !usuarioLogadoService.ehAdmin(logado)) {
            throw new EntityNotFoundException(
                    "Vacina não encontrada"
            );
        }

        return vacina;
    }

    // A vacina só aponta para uma consulta (histórico) do mesmo pet
    private Historico historicoDoPet(UUID idHistorico, Pet pet) {

        if (idHistorico == null) {
            return null;
        }

        Historico historico = historicoRepository
                .findById(idHistorico)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Histórico não encontrado"
                        )
                );

        if (historico.getPet() == null || !historico.getPet().getId().equals(pet.getId())) {
            throw new EntityNotFoundException(
                    "Histórico não encontrado"
            );
        }

        return historico;
    }
}
