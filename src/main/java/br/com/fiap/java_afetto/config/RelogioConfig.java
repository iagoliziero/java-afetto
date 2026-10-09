package br.com.fiap.java_afetto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelogioConfig {

    // "Hoje" e "agora" no fuso do Brasil. O servidor (Render) roda em UTC:
    // sem isto, uma consulta marcada para as 20h já contaria como realizada às 17h.
    @Bean
    public Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
