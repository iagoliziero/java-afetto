package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.pet.PetLista;
import br.com.fiap.java_afetto.dto.pet.PetRequest;
import br.com.fiap.java_afetto.dto.pet.PetResponse;
import br.com.fiap.java_afetto.dto.pet.PetStatusVacinacaoResponse;
import br.com.fiap.java_afetto.mapper.PetMapper;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.Vacina;
import br.com.fiap.java_afetto.model.pet.Pet;
import br.com.fiap.java_afetto.repository.HistoricoRepository;
import br.com.fiap.java_afetto.repository.PetRepository;
import br.com.fiap.java_afetto.repository.RemedioRepository;
import br.com.fiap.java_afetto.repository.UsuarioRepository;
import br.com.fiap.java_afetto.repository.VacinaRepository;
import br.com.fiap.java_afetto.model.UserRole;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final UsuarioRepository usuarioRepository;
    private final VacinaRepository vacinaRepository;
    private final RemedioRepository remedioRepository;
    private final HistoricoRepository historicoRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    public PetService(
            PetRepository petRepository,
            PetMapper petMapper,
            UsuarioRepository usuarioRepository,
            VacinaRepository vacinaRepository,
            RemedioRepository remedioRepository,
            HistoricoRepository historicoRepository,
            UsuarioLogadoService usuarioLogadoService
    ) {
        this.petRepository = petRepository;
        this.petMapper = petMapper;
        this.usuarioRepository = usuarioRepository;
        this.vacinaRepository = vacinaRepository;
        this.remedioRepository = remedioRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public PetResponse create(PetRequest petRequest) {

        Pet pet = new Pet();

        pet.setNome(petRequest.nome());
        pet.setEspecie(petRequest.especie());
        pet.setRaca(petRequest.raca());
        pet.setSexo(petRequest.sexo());
        pet.setPeso(petRequest.peso());
        pet.setDataNasc(petRequest.dataNasc());
        pet.setDescricao(petRequest.descricao());

        // O pet é sempre de quem está logado (antes valia o idUsuario do corpo,
        // e dava para criar pet na conta de outra pessoa — ou sem dono)
        pet.setUsuario(donoDoPet(petRequest.idUsuario()));

        return petMapper.petToResponse(
                petRepository.save(pet)
        );
    }

    // Pet de outra conta responde 404, igual a um id que não existe
    public PetResponse read(UUID id) {

        return petMapper.petToResponse(
                usuarioLogadoService.buscarPetPermitido(id)
        );
    }

    // Só os pets do usuário logado (antes vinham os de todas as contas); o ADMIN continua vendo todos
    public Page<PetLista> read(Pageable pageable) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        Page<Pet> pets = usuario.getRole() == UserRole.ADMIN
                ? petRepository.findAll(pageable)
                : petRepository.findByUsuario_Id(usuario.getId(), pageable);

        return pets.map(petMapper::petToResponseLista);
    }

    public PetResponse update(
            UUID id,
            PetRequest request
    ) {

        Pet pet = usuarioLogadoService.buscarPetPermitido(id);

        pet.setNome(request.nome());
        pet.setEspecie(request.especie());
        pet.setRaca(request.raca());
        pet.setSexo(request.sexo());
        pet.setPeso(request.peso());
        pet.setDataNasc(request.dataNasc());
        pet.setDescricao(request.descricao());

        // Sem idUsuario, o pet continua com o mesmo dono; só o ADMIN passa o pet para outra conta
        if (request.idUsuario() != null) {
            pet.setUsuario(donoDoPet(request.idUsuario()));
        }

        return petMapper.petToResponse(
                petRepository.save(pet)
        );
    }

    @Transactional
    public void delete(UUID id) {

        usuarioLogadoService.buscarPetPermitido(id);

        // Os cuidados do pet saem junto com ele: sem isso o banco recusa
        // apagar um pet que tem vacina, remédio ou consulta (chave estrangeira).
        // As vacinas vão primeiro porque podem apontar para uma consulta.
        vacinaRepository.deleteByPet_Id(id);
        remedioRepository.deleteByPet_Id(id);
        historicoRepository.deleteByPet_Id(id);

        petRepository.deleteById(id);
    }

    public PetStatusVacinacaoResponse statusVacinacao(UUID id) {

        Pet pet = usuarioLogadoService.buscarPetPermitido(id);

        LocalDate hoje = LocalDate.now();
        LocalDate limiteProximas = hoje.plusDays(30);

        int atrasadas = 0;
        int proximas = 0;
        int emDia = 0;

        for (Vacina vacina : pet.getVacinas()) {

            if (vacina.getProximaDose() == null) {
                emDia++;
                continue;
            }

            LocalDate proximaDose = vacina.getProximaDose();

            if (proximaDose.isBefore(hoje)) {

                atrasadas++;

            } else if (
                    !proximaDose.isAfter(limiteProximas)
            ) {

                proximas++;

            } else {

                emDia++;
            }
        }

        String status;

        if (atrasadas > 0) {
            status = "ATENCAO";
        } else if (proximas > 0) {
            status = "PROXIMA_DOSE";
        } else {
            status = "EM_DIA";
        }

        return new PetStatusVacinacaoResponse(
                pet.getNome(),
                pet.getVacinas().size(),
                atrasadas,
                proximas,
                emDia,
                status
        );
    }

    /**
     * Dono do pet: quem está logado. O idUsuario do corpo só pode ser o próprio
     * id; outra conta só o ADMIN escolhe — para os outros, a conta responde
     * "não encontrada" (404), sem revelar se ela existe.
     */
    private Usuario donoDoPet(UUID idUsuario) {

        Usuario logado = usuarioLogadoService.buscarUsuarioLogado();

        if (idUsuario == null || idUsuario.equals(logado.getId())) {
            return logado;
        }

        if (!usuarioLogadoService.ehAdmin(logado)) {
            throw new EntityNotFoundException(
                    "Usuário não encontrado"
            );
        }

        return usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Usuário não encontrado"
                        )
                );
    }
}
