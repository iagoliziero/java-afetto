package br.com.fiap.java_afetto;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base dos testes de API: sobe a aplicação inteira numa porta livre (H2 em
 * memória + migrations do Flyway) e conversa com ela por HTTP, como o app faz.
 * Cada teste cria o próprio usuário (e-mail único), então um não atrapalha o outro.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class ApiTesteBase {

    // CEPs que o serviço de CEP falso conhece, com as coordenadas que ele devolve
    protected static final String CEP_PINHEIROS = "05422-001";
    protected static final String CEP_SE = "01001-000";
    private static final Map<String, String> COORDENADAS_FALSAS = Map.of(
            "05422001", "{\"cep\":\"05422001\",\"lat\":\"-23.5660000\",\"lng\":\"-46.6845000\"}",
            "01001000", "{\"cep\":\"01001000\",\"lat\":\"-23.5502784\",\"lng\":\"-46.6342179\"}"
    );

    // Serviço de CEP falso no lugar da AwesomeAPI: os testes não dependem da internet
    private static final HttpServer CEP_FALSO = iniciarCepFalso();

    @DynamicPropertySource
    static void usarCepFalso(DynamicPropertyRegistry registry) {
        registry.add("afetto.geolocalizacao.url",
                () -> "http://localhost:" + CEP_FALSO.getAddress().getPort() + "/json/{cep}");
    }

    private static HttpServer iniciarCepFalso() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            servidor.createContext("/json/", troca -> {
                String cep = troca.getRequestURI().getPath().substring("/json/".length());
                String json = COORDENADAS_FALSAS.getOrDefault(cep, "{\"code\":\"not_found\"}");
                byte[] corpo = json.getBytes(StandardCharsets.UTF_8);
                troca.getResponseHeaders().add("Content-Type", "application/json");
                troca.sendResponseHeaders(COORDENADAS_FALSAS.containsKey(cep) ? 200 : 404, corpo.length);
                troca.getResponseBody().write(corpo);
                troca.close();
            });
            servidor.start();
            return servidor;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private Environment environment;

    private String urlBase;

    @BeforeEach
    void definirUrlBase() {
        String porta = environment.getProperty("local.server.port");
        assertNotNull(porta, "A aplicação de teste não informou a porta (local.server.port)");
        urlBase = "http://localhost:" + porta;
    }

    protected ClienteApi novoCliente() {
        return new ClienteApi(urlBase);
    }

    /** Cria um usuário novo, faz login e devolve o cliente já com a sessão. */
    protected UsuarioTeste novoUsuarioLogado() {

        ClienteApi cliente = novoCliente();
        String email = "teste-" + UUID.randomUUID() + "@afetto.com";

        ClienteApi.Resposta cadastro = cliente.post("/usuario", """
                {
                  "nome": "Tutor de Teste",
                  "cpf": "12345678909",
                  "dataNascimento": "1995-05-20",
                  "email": "%s",
                  "senha": "senha123",
                  "telefone": "11999999999"
                }
                """.formatted(email));
        assertEquals(201, cadastro.status(), cadastro.corpo());

        ClienteApi.Resposta login = cliente.post("/login", """
                { "email": "%s", "senha": "senha123" }
                """.formatted(email));
        assertEquals(200, login.status(), login.corpo());

        return new UsuarioTeste(cliente, cadastro.id(), email);
    }

    protected String novoPet(UsuarioTeste usuario, String nome) {

        ClienteApi.Resposta pet = usuario.cliente().post("/pet", """
                {
                  "nome": "%s",
                  "especie": "CACHORRO",
                  "raca": "Vira-lata",
                  "sexo": "MACHO",
                  "peso": 12.5,
                  "dataNasc": "2022-03-10",
                  "idUsuario": "%s"
                }
                """.formatted(nome, usuario.id()));
        assertEquals(201, pet.status(), pet.corpo());

        return pet.id();
    }

    protected String novaVacina(UsuarioTeste usuario, String idPet, String nomeVacina) {

        ClienteApi.Resposta vacina = usuario.cliente().post("/vacina", """
                {
                  "nomeVacina": "%s",
                  "dataAplicacao": "2026-09-01",
                  "proximaDose": "2027-09-01",
                  "idPet": "%s"
                }
                """.formatted(nomeVacina, idPet));
        assertEquals(201, vacina.status(), vacina.corpo());

        return vacina.id();
    }

    /**
     * Valor de um campo do item de uma lista, procurado a partir do id do item
     * (ex.: "perto" da clínica X). Serve para listas em que o id vem primeiro.
     */
    protected static String campoDoItem(String corpo, String idItem, String campo) {

        int inicio = corpo.indexOf("\"" + idItem + "\"");
        assertTrue(inicio >= 0, "item " + idItem + " não está na resposta: " + corpo);

        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*([^,}\\s]+)").matcher(corpo);
        assertTrue(matcher.find(inicio), "campo " + campo + " não encontrado: " + corpo);

        return matcher.group(1);
    }

    record UsuarioTeste(ClienteApi cliente, String id, String email) {
    }
}
