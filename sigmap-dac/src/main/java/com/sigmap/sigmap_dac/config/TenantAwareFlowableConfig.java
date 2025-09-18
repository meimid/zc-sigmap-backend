package com.sigmap.sigmap_dac.config;

import jakarta.annotation.PostConstruct;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

/**
 * Deploys DAC BPMN process definitions on startup for the configured tenant.
 * Scans classpath:{process.definition.location}*.bpmn20.xml, compares file checksums
 * against deployed resources for the same tenant, and deploys only when changed.
 */
@Configuration
public class TenantAwareFlowableConfig {

    /** e.g. "dac" (or the same tenant you use across modules) */
    @Value("${tenant.id}")
    private String TENANT_ID;

    /** Directory on classpath that contains *.bpmn20.xml files (e.g. "processes/dac/") */
    @Value("${process.definition.location}")
    private String PROCESS_DEFINITION_LOCATION;

    @Autowired
    private ProcessEngine processEngine;

    @PostConstruct
    public void deployProcessDefinitions() throws IOException {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:" + PROCESS_DEFINITION_LOCATION + "*.bpmn20.xml");

        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) continue;

            String bpmnXml = toString(resource.getInputStream());
            String newChecksum = DigestUtils.sha256Hex(bpmnXml);
            String processKey = fileName.replace(".bpmn20.xml", "");

            // All deployed defs for this tenant+key (latest first)
            List<ProcessDefinition> existingDefs = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionTenantId(TENANT_ID)
                    .processDefinitionKey(processKey)
                    .orderByProcessDefinitionVersion().desc()
                    .list();

            boolean alreadyDeployed = existingDefs.stream().anyMatch(def -> {
                try (InputStream deployedStream =
                             repositoryService.getResourceAsStream(def.getDeploymentId(), fileName)) {
                    if (deployedStream == null) return false;
                    String deployedXml = toString(deployedStream);
                    return newChecksum.equals(DigestUtils.sha256Hex(deployedXml));
                } catch (IOException e) {
                    return false;
                }
            });

            if (!alreadyDeployed) {
                repositoryService.createDeployment()
                        .tenantId(TENANT_ID)
                        .name("DAC Process Deployment")
                        .addInputStream(fileName, resource.getInputStream())
                        .deploy();
                System.out.println("[DAC] Deployed: " + fileName);
            } else {
                System.out.println("[DAC] Skipped (no changes): " + fileName);
            }
        }

        // Ensure future runtime operations default to the tenant
        runtimeService.createProcessInstanceBuilder().tenantId(TENANT_ID);
    }

    private String toString(InputStream stream) {
        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }
}
