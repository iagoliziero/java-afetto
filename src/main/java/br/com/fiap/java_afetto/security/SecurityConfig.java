package br.com.fiap.java_afetto.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                // App aberto no navegador (Expo web em localhost) chamando a API
                .cors(cors -> cors.configurationSource(origensLiberadas()))

                //Verificação pra ver se o flyway foi professor
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/h2-console/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/usuario")
                        .permitAll()
                        // Esqueci a senha: quem chama ainda não consegue fazer login
                        .requestMatchers(HttpMethod.POST, "/senha/redefinir")
                        .permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/usuario/*/role")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/usuario/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/pais").hasRole("ADMIN")
                        .requestMatchers("/estado").hasRole("ADMIN")
                        .requestMatchers("/cidade").hasRole("ADMIN")
                        .requestMatchers("/bairro").hasRole("ADMIN")
                        .requestMatchers("/logradouro").hasRole("ADMIN")
                        .requestMatchers("/endereco").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/pet/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/usuario/**").hasAnyRole("USER", "ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(AbstractHttpConfigurer::disable)

                .logout(LogoutConfigurer::permitAll
                );

        return http.build();
    }

    /*
     * CORS: o navegador só deixa uma página (ex.: o app em http://localhost:8082)
     * chamar a API em outra porta se a API autorizar. Liberado só para
     * localhost, em qualquer porta, com o cookie de sessão. O app no celular
     * não passa por isso (não é navegador).
     */
    private CorsConfigurationSource origensLiberadas() {

        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(List.of("http://localhost:[*]", "http://127.0.0.1:[*]"));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource origens = new UrlBasedCorsConfigurationSource();
        origens.registerCorsConfiguration("/**", configuracao);

        return origens;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}