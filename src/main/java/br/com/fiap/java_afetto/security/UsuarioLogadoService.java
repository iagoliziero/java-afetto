package br.com.fiap.java_afetto.security;

import br.com.fiap.java_afetto.model.UserRole;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.pet.Pet;
import br.com.fiap.java_afetto.repository.PetRepository;
import br.com.fiap.java_afetto.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
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

    public boolean ehAdmin(Usuario usuario) {
        return usuario.getRole() == UserRole.ADMIN;
    }

    /**
     * Busca um pet do usuário logado. Pet de outro usuário responde como
     * "não encontrado", para não revelar que ele existe.
     * Usado por remédios, consultas, calendário e agendamento.
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

    /**
     * Pet que quem está logado pode ver e mexer nas rotas /pet e /vacina: o
     * dele; o ADMIN, qualquer um. Pet de outra conta responde "não encontrado"
     * (404), igual a um id que não existe.
     */
    public Pet buscarPetPermitido(UUID idPet) {

        Usuario usuario = buscarUsuarioLogado();

        Optional<Pet> pet = ehAdmin(usuario)
                ? petRepository.findById(idPet)
                : petRepository.findByIdAndUsuario_Id(idPet, usuario.getId());

        return pet.orElseThrow(() ->
                new EntityNotFoundException(
                        "Pet não encontrado"
                )
        );
    }

    /**
     * Usuário que quem está logado pode ver e editar em /usuario/{id}: ele
     * mesmo; o ADMIN, qualquer um. Outra conta responde "não encontrado" (404).
     */
    public Usuario buscarUsuarioPermitido(UUID idUsuario) {

        Usuario logado = buscarUsuarioLogado();

        if (logado.getId().equals(idUsuario)) {
            return logado;
        }

        if (!ehAdmin(logado)) {
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

    /**
     * Depois de a pessoa trocar o próprio e-mail. A sessão guarda quem está
     * logado pelo e-mail do login: sem atualizar, as próximas chamadas
     * procuravam o e-mail antigo e respondiam "Usuário não encontrado" até um
     * novo login. Guarda na sessão do mesmo jeito que o POST /login.
     */
    public void atualizarEmailDaSessao(String novoEmail) {

        Authentication atual = SecurityContextHolder.getContext().getAuthentication();

        if (atual == null || novoEmail == null || atual.getName().equals(novoEmail)) {
            return;
        }

        Authentication nova = UsernamePasswordAuthenticationToken.authenticated(
                novoEmail, null, atual.getAuthorities()
        );

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(nova);
        SecurityContextHolder.setContext(contexto);

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                contexto
        );
    }
}
