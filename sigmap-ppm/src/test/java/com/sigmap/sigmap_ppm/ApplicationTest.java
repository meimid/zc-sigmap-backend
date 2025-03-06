package com.sigmap.sigmap_ppm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class ApplicationTest {
  static final MariaDBContainer<?> MARIA_DB_CONTAINER;

  static {
    MARIA_DB_CONTAINER = new MariaDBContainer<>("mariadb:11");
    MARIA_DB_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureTestProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MARIA_DB_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", MARIA_DB_CONTAINER::getUsername);
    registry.add("spring.datasource.password", MARIA_DB_CONTAINER::getPassword);
  }

  @Test
  void contextLoad() {}
}
