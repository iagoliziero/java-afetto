package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Os usuários de teste são criados com senha "senha123" (ApiTesteBase)
@DisplayName("Trocar a senha logado (conferindo a atual)")
class AlterarSenhaApiTests extends ApiTesteBase {

    private static String pedido(String senhaAtual, String novaSenha) {
        return """
                { "senhaAtual": "%s", "novaSenha": "%s" }
                """.formatted(senhaAtual, novaSenha);
    }

    private int login(String email, String senha) {
        return novoCliente().post("/login", """
                { "email": "%s", "senha": "%s" }
                """.formatted(email, senha)).status();
    }

    @Test
    @DisplayName("Com a senha atual certa, troca: a nova entra e a antiga não")
    void trocar() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta resposta = ana.cliente().put("/usuario/me/senha", pedido("senha123", "outraSenha456"));

        assertEquals(204, resposta.status(), resposta.corpo());
        assertEquals(200, login(ana.email(), "outraSenha456"));
        assertNotEquals(200, login(ana.email(), "senha123"));
    }

    @Test
    @DisplayName("Com a senha atual errada, recusa (400) e a senha continua a mesma")
    void senhaAtualErrada() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta resposta = ana.cliente().put("/usuario/me/senha", pedido("chute123", "outraSenha456"));

        assertEquals(400, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("A senha atual está incorreta"), resposta.corpo());
        assertEquals(200, login(ana.email(), "senha123"));
        assertNotEquals(200, login(ana.email(), "outraSenha456"));
    }

    @Test
    @DisplayName("Senha nova curta é recusada (400)")
    void senhaNovaCurta() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta resposta = ana.cliente().put("/usuario/me/senha", pedido("senha123", "123"));

        assertEquals(400, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("A nova senha deve ter pelo menos 6 caracteres"), resposta.corpo());
        assertEquals(200, login(ana.email(), "senha123"));
    }

    @Test
    @DisplayName("Sem login não troca nada")
    void semLogin() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta resposta = novoCliente().put("/usuario/me/senha", pedido("senha123", "outraSenha456"));

        assertNotEquals(204, resposta.status(), resposta.corpo());
        assertEquals(200, login(ana.email(), "senha123"));
    }

    @Test
    @DisplayName("Editar os dados (PUT /usuario/{id}) não muda mais a senha, mesmo mandando \"senha\" no corpo")
    void editarDadosNaoMexeNaSenha() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta editado = ana.cliente().put("/usuario/" + ana.id(), """
                {
                  "nome": "Ana Paula",
                  "cpf": "12345678909",
                  "dataNascimento": "1995-05-20",
                  "email": "%s",
                  "telefone": "11988887777",
                  "senha": "qualquerCoisa999"
                }
                """.formatted(ana.email()));

        assertEquals(200, editado.status(), editado.corpo());
        assertTrue(editado.corpo().contains("Ana Paula"), editado.corpo());
        assertEquals(200, login(ana.email(), "senha123"));
        assertNotEquals(200, login(ana.email(), "qualquerCoisa999"));
    }
}
