package com.sigmap.sigmap_auth;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan({"com.sigmap.common", "com.sigmap.sigmap_auth"})
@EnableJpaRepositories({"com.sigmap.common"})
@EntityScan({"com.sigmap.common"})
@SpringBootApplication
@SecurityScheme(
    name = "Keycloak",
    openIdConnectUrl =
        "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/.well-known/openid-configuration",
    type = SecuritySchemeType.OPENIDCONNECT,
    in = SecuritySchemeIn.HEADER)
@OpenAPIDefinition(
    info = @Info(version = "0.0.1"),
    security = {@SecurityRequirement(name = "Keycloak")})
public class Application {
  /**
   * Main entry point.
   *
   * @param args application arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @PostConstruct
  void bypassCert() throws Exception {
    // Create a TrustManager that accepts all certificates
    TrustManager[] trustAllCertificates =
        new TrustManager[] {
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
              // Trust all client certificates
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
              // Trust all server certificates
            }
          }
        };

    // Initialize SSLContext to accept all certificates
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
    SSLContext.setDefault(sslContext);
  }
}
