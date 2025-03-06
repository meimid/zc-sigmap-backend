package com.sigmap.sigmap_agpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan({"com.sigmap.common", "com.sigmap.sigmap_agpm"})
@EnableJpaRepositories({"com.sigmap.common"})
@EntityScan({"com.sigmap.common"})
@SpringBootApplication
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
