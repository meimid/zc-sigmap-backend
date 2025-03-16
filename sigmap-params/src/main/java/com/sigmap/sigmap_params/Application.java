package com.sigmap.sigmap_params;

import com.sigmap.common.security.SpringSecurityAuditorAware;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

@ComponentScan({"com.sigmap.common", "com.sigmap.sigmap_params"})
@EnableJpaRepositories({"com.sigmap.common"})
@EntityScan({"com.sigmap.common"})
@SpringBootApplication
@EnableJpaAuditing
public class Application {
  /**
   * Main entry point.
   *
   * @param args application arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public AuditorAware<String> auditorProvider() {
    return new SpringSecurityAuditorAware();
  }

  @Bean
  public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
    return new SecurityEvaluationContextExtension();
  }

}
