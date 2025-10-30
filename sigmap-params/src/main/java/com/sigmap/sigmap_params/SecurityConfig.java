package com.sigmap.sigmap_params;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${cors.origins}")
    private List<String> allowedOrigins;

    private final Environment env;

    public SecurityConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder, KeycloakJwtConverter jwtConverter) throws Exception {

        http.cors(
                        (cors) ->
                                cors.configurationSource(
                                        request -> {
                                            CorsConfiguration corsConfiguration = new CorsConfiguration();
                                            corsConfiguration.setAllowedOriginPatterns(allowedOrigins);
                                            corsConfiguration.setAllowedMethods(List.of("*"));
                                            corsConfiguration.setAllowedHeaders(List.of("*"));
                                            corsConfiguration.setAllowCredentials(true);
                                            return corsConfiguration;
                                        }))
                .authorizeHttpRequests(
                        request ->
                                request
                                        .requestMatchers(
                                                "/actuator/**",
                                                "/token/**",
                                                "/test",
                                                "/swagger-ui/**",
                                                "/api/swagger-ui/**",
                                                "/api/docs",
                                                "/api/docs/*",
                                                "/api/docs-ui.html",
                                                "/v3/api-docs/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .sessionManagement(
                        management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = env.getProperty("keycloak.server-url") + "/realms/" + env.getProperty("keycloak.realm");
        System.out.println("Issuer URI: " + issuerUri);

        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

        // Audiences autorisées : ici sigmap-api
        List<String> validAudiences = List.of(
                env.getProperty("keycloak.client-id"),          // sigmap-api
                env.getProperty("keycloak.common-client-id") // backend-common
        );

        jwtDecoder.setJwtValidator(token -> {
            System.out.println("VALIDATE JWT CALLED");
            System.out.println("Token aud: " + token.getAudience());

            for (String aud : token.getAudience()) {
                if (validAudiences.contains(aud)) {
                    return OAuth2TokenValidatorResult.success();
                }
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Audience invalide", null)
            );
        });

        return jwtDecoder;
    }

    @Bean
    public KeycloakJwtConverter jwtAuthenticationConverter() {
        List<String> clientIds = List.of(
                env.getProperty("keycloak.client-id"),
                env.getProperty("keycloak.common-client-id")
        ).stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());

        return new KeycloakJwtConverter(clientIds);
    }
}
