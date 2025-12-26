package com.sigmap.sigmap_agpm.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

/**
 * Configuration class for tenant-specific Flowable process deployment.
 * <p>
 * This class automatically deploys BPMN process definitions located in the configured directory
 * (specified by {@code process.definition.location}) to the Flowable engine for a specific tenant
 * (specified by {@code tenant.id}). It ensures that only new or modified process definitions are
 * deployed by comparing SHA-256 checksums of the BPMN XML files. The deployment occurs during
 * application startup, leveraging Spring Boot's {@code @PostConstruct} mechanism.
 * </p>
 * <p>
 * The class is part of a Flowable 7.1.0-based workflow management system integrated with Spring Boot
 * 3.1.5, using a dual-database setup ({@code project_db} for JPA entities and {@code flowable_db}
 * for Flowable tables). It supports tenant-specific operations for the "agpm" tenant and is optimized
 * for high-performance scenarios, such as handling 1000 concurrent process instances.
 * </p>
 *
 * @see org.flowable.engine.ProcessEngine
 * @see org.springframework.context.annotation.Configuration
 */
@Slf4j
@Configuration
public class TenantAwareFlowableConfig {

    /** Tenant ID injected from application.yml (e.g., "agpm"). */
    @Value("${tenant.id}")
    private String TENANT_ID;

    /** Directory containing BPMN process definition files, injected from application.yml (e.g., "processes/"). */
    @Value("${process.definition.location}")
    private String PROCESS_DEFINITION_LOCATION;

    /** Flowable ProcessEngine for interacting with process definitions and instances. */
    @Autowired
    private ProcessEngine processEngine;

    /**
     * Deploys BPMN process definitions during application startup.
     * <p>
     * This method scans the configured directory ({@code PROCESS_DEFINITION_LOCATION}) for
     * BPMN files (*.bpmn20.xml), calculates their SHA-256 checksums, and compares them with
     * existing deployments in the Flowable engine for the specified tenant ({@code TENANT_ID}).
     * If a process definition is new or has changed, it is deployed; otherwise, it is skipped.
     * The method ensures efficient deployment by avoiding redundant operations and supports
     * tenant-specific process management.
     * </p>
     *
     * @throws IOException if an error occurs while reading BPMN files or deploying processes
     */
    @PostConstruct
    public void deployProcessDefinitions() throws IOException {
        // Initialize Flowable services
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        // Load all BPMN files from the configured directory
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:" + PROCESS_DEFINITION_LOCATION + "*.bpmn20.xml");

        // Process each BPMN file
        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            String bpmnXml = toString(resource.getInputStream());
            String newChecksum = DigestUtils.sha256Hex(bpmnXml);
            String processKey = fileName.replace(".bpmn20.xml", "");

            // Query existing process definitions for the tenant and process key
            List<ProcessDefinition> existingDefs = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionTenantId(TENANT_ID)
                    .processDefinitionKey(processKey)
                    .orderByProcessDefinitionVersion().desc()
                    .list();

            // Check if the process definition is already deployed with the same content
            boolean alreadyDeployed = existingDefs.stream().anyMatch(def -> {
                try (InputStream deployedStream = repositoryService.getResourceAsStream(def.getDeploymentId(), fileName)) {
                    String deployedXml = toString(deployedStream);
                    return newChecksum.equals(DigestUtils.sha256Hex(deployedXml));
                } catch (IOException e) {
                    return false; // Assume not deployed if an error occurs
                }
            });

            // Deploy the process if it’s new or modified
            if (!alreadyDeployed) {
                repositoryService.createDeployment()
                        .tenantId(TENANT_ID)
                        .name("AGPM Process Deployment")
                        .addInputStream(fileName, resource.getInputStream())
                        .deploy();
                log.info("Deployed: {}" , fileName);
            } else {
                log.info("Skipped (no changes): {}" ,fileName);
            }
        }

        // Initialize a process instance builder with the tenant ID (placeholder for future use)
        runtimeService.createProcessInstanceBuilder().tenantId(TENANT_ID);
    }

    /**
     * Converts an InputStream to a String.
     * <p>
     * This utility method reads the content of an InputStream (e.g., BPMN XML) into a String
     * using UTF-8 encoding. It is used to process BPMN files and compare their content for
     * deployment purposes.
     * </p>
     *
     * @param stream the InputStream to convert
     * @return the String content of the InputStream, or an empty String if the stream is empty
     */
    private String toString(InputStream stream) {
        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }
}
