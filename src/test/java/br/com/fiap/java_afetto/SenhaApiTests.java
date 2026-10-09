package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Os usuários de teste são criados com CPF 12345678909, nascimento 1995-05-20 e senha "senha123"
@DisplayName("Passo 10 — redefinir senha")
class SenhaApiTests extends ApiTesteBase {

    private static final String DADOS_NAO_CONFEREM = "Os dados não conferem";

    private static String pedido(String email, String cpf, String dataNascimento, String novaSenha) {
        return """
                { "email": "%s", "cpf": "%s", "dataNascimento": "%s", "novaSenha": "%s" }
                """.formatted(email, cpf, dataNascimento, novaSenha);
    }

    private int login(String email, String senha) {
        return novoCliente().post("/login", """
                { "email": "%s", "senha": "%s" }
                """.formatted(email, senha)).status();
    }

    @Test
    @DisplayName("Com e-mail, CPF e nascimento certos, troca a senha sem estar logado")
    void redefinir() {

        String email = novoUsuarioLogado().email();

        // Cliente novo, sem sessão: é quem esqueceu a senha
        ClienteApi.Resposta resposta = novoCliente().post("/senha/redefinir",
                pedido(email, "123.456.789-09", "1995-05-20", "novaSenha456"));

        assertEquals(204, resposta.status(), resposta.corpo());
        assertEquals(200, login(email, "novaSenha456"));
        assertNotEquals(200, login(email, "senha123"));
    }

    @Test
    @DisplayName("Dado errado ou e-mail desconhecido: sempre a mesma resposta, e a senha não muda")
    void dadosQueNaoConferem() {

        String email = novoUsuarioLogado().email();
        ClienteApi anonimo = novoCliente();

        ClienteApi.Resposta cpfErrado = anonimo.post("/senha/redefinir", pedido(email, "00000000000", "1995-05-20", "novaSenha456"));
        assertEquals(400, cpfErrado.status(), cpfErrado.corpo());
        assertTrue(cpfErrado.corpo().contains(DADOS_NAO_CONFEREM), cpfErrado.corpo());

        ClienteApi.Resposta dataErrada = anonimo.post("/senha/redefinir", pedido(email, "12345678909", "2000-01-01", "novaSenha456"));
        assertEquals(400, dataErrada.status(), dataErrada.corpo());
        assertTrue(dataErrada.corpo().contains(DADOS_NAO_CONFEREM), dataErrada.corpo());

        ClienteApi.Resposta emailDesconhecido = anonimo.post("/senha/redefinir",
                pedido("ninguem-" + UUID.randomUUID() + "@afetto.com", "12345678909", "1995-05-20", "novaSenha456"));
        assertEquals(400, emailDesconhecido.status(), emailDesconhecido.corpo());
        assertTrue(emailDesconhecido.corpo().contains(DADOS_NAO_CONFEREM), emailDesconhecido.corpo());

        assertEquals(200, login(email, "senha123"));
    }

    @Test
    @DisplayName("Depois de 5 tentativas erradas, o e-mail fica bloqueado (429) mesmo com os dados certos")
    void muitasTentativas() {

        String email = novoUsuarioLogado().email();
        ClienteApi anonimo = novoCliente();

        for (int i = 0; i < 5; i++) {
            assertEquals(400, anonimo.post("/senha/redefinir", pedido(email, "00000000000", "1995-05-20", "novaSenha456")).status());
        }

        ClienteApi.Resposta bloqueado = anonimo.post("/senha/redefinir", pedido(email, "12345678909", "1995-05-20", "novaSenha456"));
        assertEquals(429, bloqueado.status(), bloqueado.corpo());
        assertTrue(bloqueado.corpo().contains("Muitas tentativas"), bloqueado.corpo());

        assertEquals(200, login(email, "senha123"));
    }

    @Test
    @DisplayName("Senha nova curta demais responde 400")
    void senhaCurta() {

        String email = novoUsuarioLogado().email();

        ClienteApi.Resposta resposta = novoCliente().post("/senha/redefinir", pedido(email, "12345678909", "1995-05-20", "123"));

        assertEquals(400, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("A nova senha deve ter pelo menos 6 caracteres"), resposta.corpo());
    }
}
