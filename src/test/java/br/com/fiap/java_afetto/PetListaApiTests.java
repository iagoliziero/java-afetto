package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// GET /pet listava os pets de todas as contas: uma conta nova via os pets das outras
@DisplayName("Lista de pets só da conta logada")
class PetListaApiTests extends ApiTesteBase {

    @Test
    @DisplayName("Cada conta vê só os próprios pets em GET /pet")
    void cadaContaVeOsSeusPets() {

        UsuarioTeste ana = novoUsuarioLogado();
        String thor = novoPet(ana, "Thor");

        UsuarioTeste bruno = novoUsuarioLogado();
        String rex = novoPet(bruno, "Rex");

        ClienteApi.Resposta daAna = ana.cliente().get("/pet?page=0&size=200");
        assertEquals(200, daAna.status(), daAna.corpo());
        assertTrue(daAna.corpo().contains(thor), daAna.corpo());
        assertFalse(daAna.corpo().contains(rex), daAna.corpo());

        ClienteApi.Resposta doBruno = bruno.cliente().get("/pet?page=0&size=200");
        assertEquals(200, doBruno.status(), doBruno.corpo());
        assertTrue(doBruno.corpo().contains(rex), doBruno.corpo());
        assertFalse(doBruno.corpo().contains(thor), doBruno.corpo());
    }

    @Test
    @DisplayName("Conta nova começa sem pets")
    void contaNovaSemPets() {

        novoPet(novoUsuarioLogado(), "Thor");

        ClienteApi.Resposta nova = novoUsuarioLogado().cliente().get("/pet");
        assertEquals(200, nova.status(), nova.corpo());
        assertTrue(nova.corpo().contains("\"totalElements\":0"), nova.corpo());
    }
}
