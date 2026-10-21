package br.com.fiap.java_afetto.security;

import br.com.fiap.java_afetto.model.UserRole;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Conta ADMIN criada quando a API sobe, a partir das variáveis de ambiente
 * ADMIN_EMAIL e ADMIN_SENHA (no Render: Environment; local: configuração de
 * execução do IntelliJ). Nenhuma senha fica no código nem no repositório.
 *
 * - Sem as duas variáveis, nenhuma conta ADMIN é criada.
 * - A conta é sempre ADMIN_EMAIL + ADMIN_SENHA: se já existir uma conta com
 *   esse e-mail, ela vira ADMIN e a senha passa a ser a ADMIN_SENHA.
 * - Roda antes de a API começar a atender (afterSingletonsInstantiated), então
 *   ninguém consegue cadastrar esse e-mail antes.
 * - O H2 é em memória: a cada vez que a API sobe, a conta é criada de novo.
 */
@Component
public class ContaAdminInicial implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(ContaAdminInicial.class);
    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public ContaAdminInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${afetto.admin.email:}") String email,
            @Value("${afetto.admin.senha:}") String senha
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.senha = senha == null ? "" : senha;
    }

    @Override
    public void afterSingletonsInstantiated() {

        if (email.isEmpty() || senha.isEmpty()) {
            log.info("ADMIN_EMAIL/ADMIN_SENHA não definidas: nenhuma conta ADMIN criada");
            return;
        }

        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            log.warn("ADMIN_SENHA tem menos de {} caracteres: a conta ADMIN não foi criada", TAMANHO_MINIMO_SENHA);
            return;
        }

        Usuario admin = usuarioRepository
                .findFirstByEmailIgnoreCase(email)
                .orElseGet(Usuario::new);

        if (admin.getId() == null) {
            admin.setNome("Administrador");
            admin.setEmail(email);
        }

        admin.setSenha(passwordEncoder.encode(senha));
        admin.setRole(UserRole.ADMIN);
        usuarioRepository.save(admin);

        log.info("Conta ADMIN pronta: {}", email);
    }
}
