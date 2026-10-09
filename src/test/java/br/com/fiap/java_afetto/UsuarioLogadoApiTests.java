package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Passo 1 — usuário logado, 404 e vacinas por pet")
class UsuarioLogadoApiTests extends ApiTesteBase {

    @Test
    @DisplayName("GET /usuario/me devolve o usuário da sessão")
    void meDevolveOUsuarioLogado() {

        UsuarioTeste usuario = novoUsuarioLogado();

        ClienteApi.Resposta me = usuario.cliente().get("/usuario/me");

        assertEquals(200, me.status(), me.corpo());
        assertEquals(usuario.id(), me.id(), me.corpo());
        assertTrue(me.corpo().contains(usuario.email()), me.corpo());
    }

    @Test
    @DisplayName("Sem login, GET /usuario/me é recusado")
    void semLoginNaoAcessa() {

        ClienteApi.Resposta me = novoCliente().get("/usuario/me");

        assertTrue(me.status() == 401 || me.status() == 403, "status " + me.status() + ": " + me.corpo());
    }

    @Test
    @DisplayName("Id que não existe responde 404 com a mensagem de erro")
    void idInexistenteResponde404() {

        UsuarioTeste usuario = novoUsuarioLogado();

        ClienteApi.Resposta resposta = usuario.cliente().get("/usuario/" + UUID.randomUUID());

        assertEquals(404, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("Usuário não encontrado"), resposta.corpo());
    }

    @Test
    @DisplayName("GET /vacina?idPet= devolve só as vacinas daquele pet")
    void vacinasFiltradasPorPet() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet1 = novoPet(usuario, "Thor");
        String idPet2 = novoPet(usuario, "Mel");
        String vacinaDoPet1 = "V10-" + UUID.randomUUID();
        String vacinaDoPet2 = "Antirrabica-" + UUID.randomUUID();
        novaVacina(usuario, idPet1, vacinaDoPet1);
        novaVacina(usuario, idPet2, vacinaDoPet2);

        ClienteApi.Resposta lista = usuario.cliente().get("/vacina?idPet=" + idPet1);

        assertEquals(200, lista.status(), lista.corpo());
        assertTrue(lista.corpo().contains(vacinaDoPet1), lista.corpo());
        assertFalse(lista.corpo().contains(vacinaDoPet2), lista.corpo());
    }
}
