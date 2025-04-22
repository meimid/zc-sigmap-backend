package com.sigmap.sigmap_ppm.config;

import jakarta.annotation.PostConstruct;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.DeploymentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

@Configuration
public class TenantAwareFlowableConfig {

    private static final String TENANT_ID = "ppm";
    private static final String PROCESS_DEFINITION_LOCATION = "processes/";

    @Autowired
    private ProcessEngine processEngine;

    @PostConstruct
    public void deployProcessDefinitions() throws IOException {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        // Find all BPMN files in the processes directory
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:" + PROCESS_DEFINITION_LOCATION + "*.bpmn20.xml");

        // Deploy each process with the tenant ID
        for (Resource resource : resources) {
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                .tenantId(TENANT_ID)
                .name("AGPM Process Deployment")
                .addInputStream(resource.getFilename(), resource.getInputStream());

            deploymentBuilder.deploy();
            System.out.println("Deployed process " + resource.getFilename() + " with tenant ID: " + TENANT_ID);
        }

        // Set up a tenant filter for runtime operations
        runtimeService.createProcessInstanceBuilder()
            .tenantId(TENANT_ID);
    }
}
