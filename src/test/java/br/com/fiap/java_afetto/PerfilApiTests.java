package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Passo 9 — finalizar cadastro")
class PerfilApiTests extends ApiTesteBase {

    private static final String PINHEIROS = "c1a00000-0000-4000-8000-000000000001";
    private static final String TATUAPE = "c1a00000-0000-4000-8000-000000000004";

    private static String perfilJson(int quantidadePets, String cep, String logradouro, String bairro) {
        return """
                {
                  "tipoMoradia": "APARTAMENTO",
                  "telaProtecao": true,
                  "quantidadePets": %d,
                  "endereco": {
                    "cep": "%s",
                    "logradouro": "%s",
                    "numero": "500",
                    "complemento": "Apto 12",
                    "bairro": "%s",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  }
                }
                """.formatted(quantidadePets, cep, logradouro, bairro);
    }

    private static boolean temCampo(String corpo, String campo, String valor) {
        return Pattern.compile("\"" + campo + "\"\\s*:\\s*" + Pattern.quote(valor)).matcher(corpo).find();
    }

    @Test
    @DisplayName("Usuário novo começa com o perfil incompleto")
    void perfilNovo() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta perfil = cliente.get("/usuario/me/perfil");

        assertEquals(200, perfil.status(), perfil.corpo());
        assertTrue(temCampo(perfil.corpo(), "perfilCompleto", "false"), perfil.corpo());
        assertTrue(temCampo(perfil.corpo(), "quantidadePets", "0"), perfil.corpo());
        assertTrue(temCampo(perfil.corpo(), "endereco", "null"), perfil.corpo());
    }

    @Test
    @DisplayName("Finalizar cadastro guarda moradia, tela, endereço e as coordenadas do CEP")
    void finalizarCadastro() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta salvo = cliente.put("/usuario/me/perfil",
                perfilJson(1, "05422001", "Rua dos Pinheiros", "Pinheiros"));
        assertEquals(200, salvo.status(), salvo.corpo());

        ClienteApi.Resposta perfil = cliente.get("/usuario/me/perfil");
        String corpo = perfil.corpo();
        assertEquals(200, perfil.status(), corpo);
        assertTrue(temCampo(corpo, "perfilCompleto", "true"), corpo);
        assertTrue(temCampo(corpo, "tipoMoradia", "\"APARTAMENTO\""), corpo);
        assertTrue(temCampo(corpo, "telaProtecao", "true"), corpo);
        assertTrue(temCampo(corpo, "cep", "\"05422-001\""), corpo);
        assertTrue(corpo.contains("Rua dos Pinheiros"), corpo);
        assertTrue(corpo.contains("Apto 12"), corpo);
        assertTrue(temCampo(corpo, "estado", "\"SP\""), corpo);
        assertTrue(corpo.contains("-23.566"), corpo);
    }

    @Test
    @DisplayName("Com o endereço salvo, as clínicas perto de casa vêm primeiro sem o app mandar localização")
    void clinicasPertoDeCasa() {

        ClienteApi cliente = novoUsuarioLogado().cliente();
        cliente.put("/usuario/me/perfil", perfilJson(1, CEP_PINHEIROS, "Rua dos Pinheiros", "Pinheiros"));

        ClienteApi.Resposta lista = cliente.get("/clinica");

        assertEquals(200, lista.status(), lista.corpo());
        assertTrue(lista.corpo().indexOf(PINHEIROS) < lista.corpo().indexOf(TATUAPE), lista.corpo());
        assertEquals("0.0", campoDoItem(lista.corpo(), PINHEIROS, "distanciaKm"));
        assertEquals("true", campoDoItem(lista.corpo(), PINHEIROS, "perto"));
        assertEquals("false", campoDoItem(lista.corpo(), TATUAPE, "perto"));
    }

    @Test
    @DisplayName("Sem coordenadas para o CEP, \"perto\" vira mesma cidade")
    void cepSemCoordenadas() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta salvo = cliente.put("/usuario/me/perfil",
                perfilJson(1, "04567-890", "Rua Inventada", "Bairro Novo"));
        assertEquals(200, salvo.status(), salvo.corpo());
        assertTrue(temCampo(salvo.corpo(), "latitude", "null"), salvo.corpo());

        ClienteApi.Resposta lista = cliente.get("/clinica");
        assertEquals("null", campoDoItem(lista.corpo(), TATUAPE, "distanciaKm"));
        assertEquals("true", campoDoItem(lista.corpo(), TATUAPE, "perto"));
    }

    @Test
    @DisplayName("A quantidade de pets acompanha os pets cadastrados (informou 0, cadastrou 1, vira 1)")
    void quantidadeDePetsAutomatica() {

        UsuarioTeste tutor = novoUsuarioLogado();
        ClienteApi cliente = tutor.cliente();
        cliente.put("/usuario/me/perfil", perfilJson(0, CEP_SE, "Praça da Sé", "Sé"));

        assertTrue(temCampo(cliente.get("/usuario/me/perfil").corpo(), "quantidadePets", "0"));

        novoPet(tutor, "Thor");

        ClienteApi.Resposta depoisDoPet = cliente.get("/usuario/me/perfil");
        assertTrue(temCampo(depoisDoPet.corpo(), "quantidadePets", "1"), depoisDoPet.corpo());
        assertTrue(temCampo(depoisDoPet.corpo(), "petsCadastrados", "1"), depoisDoPet.corpo());
        assertTrue(temCampo(depoisDoPet.corpo(), "quantidadePetsInformada", "0"), depoisDoPet.corpo());

        // Informou 3 (tem pets que não cadastrou no app): vale o maior
        cliente.put("/usuario/me/perfil", perfilJson(3, CEP_SE, "Praça da Sé", "Sé"));
        assertTrue(temCampo(cliente.get("/usuario/me/perfil").corpo(), "quantidadePets", "3"));
    }

    @Test
    @DisplayName("Atualizar troca o endereço salvo")
    void atualizarEndereco() {

        ClienteApi cliente = novoUsuarioLogado().cliente();
        cliente.put("/usuario/me/perfil", perfilJson(1, CEP_SE, "Praça da Sé", "Sé"));
        cliente.put("/usuario/me/perfil", perfilJson(1, CEP_PINHEIROS, "Rua dos Pinheiros", "Pinheiros"));

        ClienteApi.Resposta perfil = cliente.get("/usuario/me/perfil");

        assertTrue(perfil.corpo().contains("Rua dos Pinheiros"), perfil.corpo());
        assertTrue(!perfil.corpo().contains("Praça da Sé"), perfil.corpo());
    }

    @Test
    @DisplayName("Dados inválidos respondem 400 com a mensagem")
    void validacoes() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta semMoradia = cliente.put("/usuario/me/perfil",
                perfilJson(1, CEP_SE, "Praça da Sé", "Sé").replace("\"tipoMoradia\": \"APARTAMENTO\",", ""));
        assertEquals(400, semMoradia.status(), semMoradia.corpo());
        assertTrue(semMoradia.corpo().contains("Selecione o tipo de moradia"), semMoradia.corpo());

        ClienteApi.Resposta cepRuim = cliente.put("/usuario/me/perfil", perfilJson(1, "123", "Praça da Sé", "Sé"));
        assertEquals(400, cepRuim.status(), cepRuim.corpo());
        assertTrue(cepRuim.corpo().contains("CEP inválido"), cepRuim.corpo());

        ClienteApi.Resposta ufQueNaoExiste = cliente.put("/usuario/me/perfil",
                perfilJson(1, CEP_SE, "Praça da Sé", "Sé").replace("\"SP\"", "\"XX\""));
        assertEquals(400, ufQueNaoExiste.status(), ufQueNaoExiste.corpo());
        assertTrue(ufQueNaoExiste.corpo().contains("UF inválida"), ufQueNaoExiste.corpo());

        ClienteApi.Resposta negativo = cliente.put("/usuario/me/perfil", perfilJson(-1, CEP_SE, "Praça da Sé", "Sé"));
        assertEquals(400, negativo.status(), negativo.corpo());
    }
}
