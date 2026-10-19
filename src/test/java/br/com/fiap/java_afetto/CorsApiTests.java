package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Antes de chamar a API de outra porta, o navegador pergunta se pode (OPTIONS, o "preflight")
@DisplayName("App no navegador (CORS)")
class CorsApiTests extends ApiTesteBase {

    @Autowired
    private Environment ambiente;

    private HttpResponse<String> perguntarSePode(String origem) throws Exception {

        String url = "http://localhost:" + ambiente.getProperty("local.server.port") + "/pet";

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", origem)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .build();

        return HttpClient.newHttpClient().send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("O app em localhost (qualquer porta) pode chamar a API, com o cookie de sessão")
    void localhostLiberado() throws Exception {

        HttpResponse<String> resposta = perguntarSePode("http://localhost:8082");

        assertEquals(200, resposta.statusCode(), resposta.body());
        assertEquals("http://localhost:8082", resposta.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
        assertEquals("true", resposta.headers().firstValue("Access-Control-Allow-Credentials").orElse(null));
    }

    @Test
    @DisplayName("Um site qualquer não pode")
    void outroSiteBloqueado() throws Exception {

        HttpResponse<String> resposta = perguntarSePode("https://site-qualquer.com");

        assertEquals(403, resposta.statusCode(), resposta.body());
        assertTrue(resposta.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }
}
