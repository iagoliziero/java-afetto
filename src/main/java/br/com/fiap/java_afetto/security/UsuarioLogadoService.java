package br.com.fiap.java_afetto.security;

import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.pet.Pet;
import br.com.fiap.java_afetto.repository.PetRepository;
import br.com.fiap.java_afetto.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Descobre quem está logado a partir da sessão (o e-mail usado no POST /login).
 * Os endpoints novos usam isto em vez de receber o idUsuario no corpo.
 */
@Service
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;
    private final PetRepository petRepository;

    public UsuarioLogadoService(
            UsuarioRepository usuarioRepository,
            PetRepository petRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.petRepository = petRepository;
    }

    public Usuario buscarUsuarioLogado() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new EntityNotFoundException(
                    "Usuário não encontrado"
            );
        }

        return usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Usuário não encontrado"
                        )
                );
    }

    /**
     * Busca um pet do usuário logado. Pet de outro usuário responde como
     * "não encontrado", para não revelar que ele existe.
     */
    public Pet buscarPetDoUsuario(UUID idPet) {

        Usuario usuario = buscarUsuarioLogado();

        return petRepository
                .findByIdAndUsuario_Id(idPet, usuario.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Pet não encontrado"
                        )
                );
    }
}
