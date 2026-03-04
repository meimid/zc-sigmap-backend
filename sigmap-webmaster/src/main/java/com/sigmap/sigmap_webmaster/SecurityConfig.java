package com.sigmap.sigmap_webmaster;

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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

/** Spring Security configuration. */
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

  /**
   * Configures the security filter chain.
   *
   * @param http the HTTP security
   * @return the security filter chain
   * @throws Exception the exception
   */
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
        .authorizeHttpRequests(request -> request
                .requestMatchers(
                        "/ws/**",
                        "/token/**",
                        "/test",
                        "/swagger-ui/**",
                        "/api/swagger-ui/**",
                        "/api/docs",
                        "/api/docs/*",
                        "/api/docs-ui.html",
                        "/v3/api-docs/**",
                        "/api/soumissionnaire",
                        "/web/api/**")
                .permitAll()
                .requestMatchers("/api/espace-societe/**").authenticated()
                .anyRequest()
                .permitAll())
        .sessionManagement(
            management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                            .decoder(jwtDecoder)
                            .jwtAuthenticationConverter(jwtConverter)
                    )
            );
      http.csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = env.getProperty("keycloak.server-url") + "/realms/" + env.getProperty("keycloak.realm");
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

        // Audiences autorisées : ici sigmap-api
        List<String> validAudiences = List.of(
                env.getProperty("keycloak.client-id"),          // sigmap-api
                env.getProperty("keycloak.common-client-id") // backend-common
        );

        jwtDecoder.setJwtValidator(token -> {
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
  /**
   * Configures the JWT converter.
   *
   * @param env the environment
   * @return the JWT converter
   */
  @Bean
  public KeycloakJwtConverter jwtAuthenticationConverter(Environment env) {
      // Lis les propriétés individuellement
      String mainClientId = env.getProperty("keycloak.client-id", "");
      String commonClientId = env.getProperty("keycloak.common-client-id", "");

      // Fusionne dans une seule liste
      List<String> clientIds = Arrays.asList(mainClientId, commonClientId).stream()
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());

      return new KeycloakJwtConverter(clientIds);
  }
}
