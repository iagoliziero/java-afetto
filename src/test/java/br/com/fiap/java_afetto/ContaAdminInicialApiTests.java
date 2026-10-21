package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A conta ADMIN vem de ADMIN_EMAIL e ADMIN_SENHA (aqui, as propriedades equivalentes). */
@DisplayName("Conta ADMIN criada pelas variáveis de ambiente")
@TestPropertySource(properties = {
        "afetto.admin.email=admin-teste@afetto.com",
        "afetto.admin.senha=senhaAdmin123"
})
class ContaAdminInicialApiTests extends ApiTesteBase {

    private ClienteApi adminLogado() {

        ClienteApi admin = novoCliente();
        ClienteApi.Resposta login = admin.post("/login", """
                { "email": "admin-teste@afetto.com", "senha": "senhaAdmin123" }
                """);
        assertEquals(200, login.status(), login.corpo());

        return admin;
    }

    @Test
    @DisplayName("O ADMIN entra, lista as contas e vê o pet de qualquer conta")
    void adminEntraEVeTudo() {

        UsuarioTeste ana = novoUsuarioLogado();
        String petDaAna = novoPet(ana, "Thor");
        ClienteApi admin = adminLogado();

        // Listar todas as contas é só do ADMIN
        assertEquals(200, admin.get("/usuario").status());
        assertEquals(200, admin.get("/usuario/" + ana.id()).status());
        assertEquals(200, admin.get("/pet/" + petDaAna).status());
    }

    @Test
    @DisplayName("Só o ADMIN troca a role de uma conta")
    void adminTrocaRole() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta promover = adminLogado().patch("/usuario/" + ana.id() + "/role", """
                { "role": "ADMIN" }
                """);

        assertEquals(200, promover.status(), promover.corpo());

        // Entrando de novo, a Ana já pode o que é de ADMIN
        ClienteApi anaDeNovo = novoCliente();
        assertEquals(200, anaDeNovo.post("/login", """
                { "email": "%s", "senha": "senha123" }
                """.formatted(ana.email())).status());
        assertEquals(200, anaDeNovo.get("/usuario").status());
    }
}
