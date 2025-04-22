package com.sigmap.sigmap_agpm.config;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FlowableInitializer {

    @Value("${flowable.datasource.url}")
    private String jdbcUrl;

    @Value("${flowable.datasource.username}")
    private String jdbcUsername;

    @Value("${flowable.datasource.password}")
    private String jdbcPassword;

    @Value("${flowable.datasource.driver-class-name}")
    private String jdbcDriver;

    @Bean(name = "processEngine")
    @Primary
    public ProcessEngine processEngine() {
        System.out.println("Initializing Flowable Process Engine with URL: " + jdbcUrl);
        
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
            .setJdbcUrl(jdbcUrl)
            .setJdbcUsername(jdbcUsername)
            .setJdbcPassword(jdbcPassword)
            .setJdbcDriver(jdbcDriver)
            .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();
        System.out.println("Flowable Process Engine initialized: " + processEngine.getName());
        
        return processEngine;
    }
}
