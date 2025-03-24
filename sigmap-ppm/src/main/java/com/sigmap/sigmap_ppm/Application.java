package com.sigmap.sigmap_ppm;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan({"com.sigmap.common", "com.sigmap.sigmap_ppm"})
@EnableJpaRepositories({"com.sigmap.common"})
@EntityScan({"com.sigmap.common"})
@SpringBootApplication
@EnableJpaAuditing
@SecurityScheme(
        name = "Keycloak",
        openIdConnectUrl = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/.well-known/openid-configuration",
        type = SecuritySchemeType.OPENIDCONNECT,
        in = SecuritySchemeIn.HEADER
)
@OpenAPIDefinition(info = @Info(version = "0.0.1"), security = { @SecurityRequirement(name = "Keycloak") })
public class Application {
  /**
   * Main entry point.
   *
   * @param args application arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
