package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Passo 4 — clínicas")
class ClinicaApiTests extends ApiTesteBase {

    // Clínicas fictícias carregadas pela migration V5
    private static final String PINHEIROS = "c1a00000-0000-4000-8000-000000000001";
    private static final String TATUAPE = "c1a00000-0000-4000-8000-000000000004";
    private static final String LOCALIZACAO_EM_PINHEIROS = "latitude=-23.5660&longitude=-46.6845";

    @Test
    @DisplayName("Lista as 8 clínicas de exemplo com foto e endereço")
    void listaAsClinicas() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta lista = cliente.get("/clinica");

        assertEquals(200, lista.status(), lista.corpo());
        for (int i = 1; i <= 8; i++) {
            String id = "c1a00000-0000-4000-8000-00000000000" + i;
            assertTrue(lista.corpo().contains(id), "faltou a clínica " + id + ": " + lista.corpo());
        }
        assertTrue(lista.corpo().contains("Hospital Veterinário Lua Cheia"), lista.corpo());
        assertTrue(lista.corpo().contains("images.pexels.com"), lista.corpo());
        assertTrue(lista.corpo().contains("Rua dos Pinheiros"), lista.corpo());
    }

    @Test
    @DisplayName("Sem localização nem endereço, não há distância nem \"perto\"")
    void semLocalizacao() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta lista = cliente.get("/clinica");

        assertEquals(200, lista.status(), lista.corpo());
        assertEquals("null", campoDoItem(lista.corpo(), PINHEIROS, "distanciaKm"));
        assertFalse(Pattern.compile("\"perto\"\\s*:\\s*true").matcher(lista.corpo()).find(), lista.corpo());
    }

    @Test
    @DisplayName("Com a localização do app, a mais perto vem primeiro e marca \"perto\"")
    void maisPertoPrimeiro() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta lista = cliente.get("/clinica?" + LOCALIZACAO_EM_PINHEIROS);

        assertEquals(200, lista.status(), lista.corpo());
        assertTrue(lista.corpo().indexOf(PINHEIROS) < lista.corpo().indexOf(TATUAPE), lista.corpo());
        assertEquals("0.0", campoDoItem(lista.corpo(), PINHEIROS, "distanciaKm"));
        assertEquals("true", campoDoItem(lista.corpo(), PINHEIROS, "perto"));
        assertEquals("11.4", campoDoItem(lista.corpo(), TATUAPE, "distanciaKm"));
        assertEquals("false", campoDoItem(lista.corpo(), TATUAPE, "perto"));
    }

    @Test
    @DisplayName("Busca pelo nome")
    void buscaPorNome() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta lista = cliente.get("/clinica?nome=lua");

        assertEquals(200, lista.status(), lista.corpo());
        assertTrue(lista.corpo().contains("Hospital Veterinário Lua Cheia"), lista.corpo());
        assertFalse(lista.corpo().contains(PINHEIROS), lista.corpo());
    }

    @Test
    @DisplayName("Detalhe traz descrição, contatos, endereço e equipe")
    void detalheDaClinica() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta detalhe = cliente.get("/clinica/" + PINHEIROS + "?" + LOCALIZACAO_EM_PINHEIROS);

        assertEquals(200, detalhe.status(), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("Clínica Veterinária Bigode e Rabo"), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("(11) 0000-0001"), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("contato@bigodeerabo.example"), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("https://www.bigodeerabo.example"), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("Pinheiros"), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("\"SP\""), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("Dra. Camila Ferraz"), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("Dr. Rafael Nunes"), detalhe.corpo());
        assertTrue(Pattern.compile("\"perto\"\\s*:\\s*true").matcher(detalhe.corpo()).find(), detalhe.corpo());
    }

    @Test
    @DisplayName("Clínica que não existe responde 404")
    void clinicaInexistente() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta resposta = cliente.get("/clinica/" + UUID.randomUUID());

        assertEquals(404, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("Clínica não encontrada"), resposta.corpo());
    }

    @Test
    @DisplayName("Sem login, a lista de clínicas é recusada")
    void semLogin() {

        ClienteApi.Resposta resposta = novoCliente().get("/clinica");

        assertTrue(resposta.status() == 401 || resposta.status() == 403, "status " + resposta.status());
    }
}
