package com.sigmap.sigmap_agpm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@Import(MailSenderTestConfig.class)
public class ApplicationTest {
  static final MariaDBContainer<?> MARIA_DB_CONTAINER;
  static final MariaDBContainer<?> FLOWABLE_DB_CONTAINER;

  static {
    MARIA_DB_CONTAINER = new MariaDBContainer<>("mariadb:11");
    MARIA_DB_CONTAINER.start();

    FLOWABLE_DB_CONTAINER = new MariaDBContainer<>("mariadb:11");
    FLOWABLE_DB_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureTestProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MARIA_DB_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", MARIA_DB_CONTAINER::getUsername);
    registry.add("spring.datasource.password", MARIA_DB_CONTAINER::getPassword);

    registry.add("flowable.datasource.url", FLOWABLE_DB_CONTAINER::getJdbcUrl);
    registry.add("flowable.datasource.username", FLOWABLE_DB_CONTAINER::getUsername);
    registry.add("flowable.datasource.password", FLOWABLE_DB_CONTAINER::getPassword);
  }

  @Test
  void contextLoad() {}
}
