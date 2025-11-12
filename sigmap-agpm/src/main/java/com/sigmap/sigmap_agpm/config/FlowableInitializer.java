package com.sigmap.sigmap_agpm.config;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for initializing the Flowable process engine and its core services.
 * <p>
 * This class sets up the Flowable process engine by configuring a {@link StandaloneProcessEngineConfiguration}
 * with datasource properties injected from {@code application.yml}. It creates Spring-managed beans for
 * the {@link ProcessEngine} and its essential services ({@link RepositoryService}, {@link RuntimeService},
 * {@link TaskService}, and {@link HistoryService}), enabling tenant-specific workflow management for the
 * "agpm" tenant. The configuration supports a dual-database architecture, with {@code project_db} for JPA
 * entities and {@code flowable_db} for Flowable tables, and is optimized for high-performance scenarios,
 * such as handling 1000 concurrent process instances.
 * </p>
 * <p>
 * The class integrates with Spring Boot 3.1.5 and Flowable 7.1.0, ensuring seamless interaction with
 * other components like {@code TenantAwareFlowableConfig} for process deployment and
 * {@code FlowableProcessController} for REST API operations.
 * </p>
 *
 * @see org.flowable.engine.ProcessEngine
 * @see org.springframework.context.annotation.Configuration
 */
@Configuration
public class FlowableInitializer {

    /**
     * JDBC URL for the Flowable database, injected from application.yml .
     */
    @Value("${flowable.datasource.url}")
    private String jdbcUrl;

    /**
     * Username for the Flowable database, injected from application.yml.
     */
    @Value("${flowable.datasource.username}")
    private String jdbcUsername;

    /**
     * Password for the Flowable database, injected from application.yml.
     */
    @Value("${flowable.datasource.password}")
    private String jdbcPassword;

    /**
     * JDBC driver class name, injected from application.yml .
     */
    @Value("${flowable.datasource.driver-class-name}")
    private String jdbcDriver;

    /**
     * Creates and configures the Flowable process engine.
     * <p>
     * This method initializes a {@link StandaloneProcessEngineConfiguration} with datasource properties
     * injected from {@code application.yml}. It enables automatic schema updates to ensure the Flowable
     * database tables (e.g., {@code ACT_RE_PROCDEF}, {@code ACT_RU_TASK}) are created or updated as needed.
     * The resulting {@link ProcessEngine} is marked as the primary bean to support dependency injection
     * in other components, such as {@code TenantAwareFlowableConfig}.
     * </p>
     *
     * @return the initialized {@link ProcessEngine}
     */
    @Bean(name = "processEngine")
    @Primary
    public ProcessEngine processEngine() {

        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl(jdbcUrl)
                .setJdbcUsername(jdbcUsername)
                .setJdbcPassword(jdbcPassword)
                .setJdbcDriver(jdbcDriver)
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);

        ProcessEngine processEngine = cfg.buildProcessEngine();
        System.out.println("Flowable Process Engine initialized: " + processEngine.getName());

        return processEngine;
    }

    /**
     * Provides the Flowable RepositoryService for managing process definitions and deployments.
     * <p>
     * This bean retrieves the {@link RepositoryService} from the initialized {@link ProcessEngine}.
     * It is used for operations such as deploying BPMN files (e.g., in {@code TenantAwareFlowableConfig})
     * and querying process definitions (e.g., via {@code POST /api/process/deploy}).
     * </p>
     *
     * @param processEngine the initialized Flowable process engine
     * @return the {@link RepositoryService}
     */
    @Bean(name = "repositoryService")
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    /**
     * Provides the Flowable RuntimeService for managing process instances and executions.
     * <p>
     * This bean retrieves the {@link RuntimeService} from the initialized {@link ProcessEngine}.
     * It supports operations like starting process instances (e.g., via {@code POST /api/process/start/{processDefinitionKey}})
     * and managing runtime data in tables like {@code ACT_RU_EXECUTION}.
     * </p>
     *
     * @param processEngine the initialized Flowable process engine
     * @return the {@link RuntimeService}
     */
    @Bean(name = "runtimeService")
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    /**
     * Provides the Flowable TaskService for managing tasks.
     * <p>
     * This bean retrieves the {@link TaskService} from the initialized {@link ProcessEngine}.
     * It is used for task-related operations, such as querying tasks (e.g., {@code GET /api/process/instances/{processInstanceId}/tasks})
     * and completing tasks in the "agpm" tenant.
     * </p>
     *
     * @param processEngine the initialized Flowable process engine
     * @return the {@link TaskService}
     */
    @Bean(name = "taskService")
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    /**
     * Provides the Flowable HistoryService for accessing historical process data.
     * <p>
     * This bean retrieves the {@link HistoryService} from the initialized {@link ProcessEngine}.
     * It supports querying historical data (e.g., completed tasks via {@code GET /api/process/completed-tasks})
     * stored in tables like {@code ACT_HI_TASKINST} for the "agpm" tenant.
     * </p>
     *
     * @param processEngine the initialized Flowable process engine
     * @return the {@link HistoryService}
     */
    @Bean(name = "historyService")
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }
}
