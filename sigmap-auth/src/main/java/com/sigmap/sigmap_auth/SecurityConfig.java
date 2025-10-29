package com.sigmap.sigmap_auth;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

/** Spring Security configuration. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${cors.origins}")
  private List<String> allowedOrigins;

  /**
   * Configures the security filter chain.
   *
   * @param http the HTTP security
   * @return the security filter chain
   * @throws Exception the exception
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, KeycloakJwtConverter jwtConverter)
      throws Exception {
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
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtConverter)));
    //                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    return http.build();
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
      String commonClientId = env.getProperty("keycloak.common-backend-client-id", "");

      // Fusionne dans une seule liste
      List<String> clientIds = Arrays.asList(mainClientId, commonClientId).stream()
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());

      return new KeycloakJwtConverter(clientIds);
  }
}
