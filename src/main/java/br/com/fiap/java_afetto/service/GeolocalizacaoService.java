package br.com.fiap.java_afetto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Latitude e longitude de um CEP, para medir a distância até as clínicas.
 * Usa um serviço público gratuito (AwesomeAPI). Se ele não responder ou não
 * conhecer o CEP, o cadastro segue sem coordenadas (o "perto de você" passa
 * a considerar a cidade). Para desligar, deixe afetto.geolocalizacao.url vazia.
 */
@Service
public class GeolocalizacaoService {

    private final String url;
    private final RestClient restClient;

    public GeolocalizacaoService(
            @Value("${afetto.geolocalizacao.url:}") String url
    ) {
        this.url = url;

        // Nunca deixar o cadastro esperando: no máximo 3 segundos
        SimpleClientHttpRequestFactory tempoLimite = new SimpleClientHttpRequestFactory();
        tempoLimite.setConnectTimeout(Duration.ofSeconds(3));
        tempoLimite.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .requestFactory(tempoLimite)
                .build();
    }

    public Optional<Coordenadas> buscarPorCep(String cep) {

        if (url == null || url.isBlank() || cep == null) {
            return Optional.empty();
        }

        try {
            Map<?, ?> resposta = restClient.get()
                    .uri(url, cep.replaceAll("\\D", ""))
                    .retrieve()
                    .body(Map.class);

            if (resposta == null || resposta.get("lat") == null || resposta.get("lng") == null) {
                return Optional.empty();
            }

            return Optional.of(new Coordenadas(
                    new BigDecimal(resposta.get("lat").toString()),
                    new BigDecimal(resposta.get("lng").toString())
            ));

        } catch (RuntimeException e) {
            // Serviço fora do ar, CEP desconhecido ou resposta estranha: segue sem coordenadas
            return Optional.empty();
        }
    }

    public record Coordenadas(BigDecimal latitude, BigDecimal longitude) {
    }
}
