//package com.sigmap.sigmap_agpm.config;
//
//import org.flowable.engine.ProcessEngine;
//import org.flowable.engine.RepositoryService;
//import org.flowable.engine.RuntimeService;
//import org.flowable.engine.repository.DeploymentBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import jakarta.annotation.PostConstruct;
//import java.io.IOException;
//
//@Configuration
//public class TenantAwareFlowableConfig {
//
//    private static final String TENANT_ID = "agpm";
//    private static final String PROCESS_DEFINITION_LOCATION = "processes/";
//
//    @Autowired
//    private ProcessEngine processEngine;
//
//    @PostConstruct
//    public void deployProcessDefinitions() throws IOException {
//        RepositoryService repositoryService = processEngine.getRepositoryService();
//        RuntimeService runtimeService = processEngine.getRuntimeService();
//
//        // Find all BPMN files in the processes directory
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource[] resources = resolver.getResources("classpath:" + PROCESS_DEFINITION_LOCATION + "*.bpmn20.xml");
//
//        // Deploy each process with the tenant ID
//        for (Resource resource : resources) {
//            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
//                    .tenantId(TENANT_ID)
//                    .name("AGPM Process Deployment")
//                    .addInputStream(resource.getFilename(), resource.getInputStream());
//
//            deploymentBuilder.deploy();
//            System.out.println("Deployed process " + resource.getFilename() + " with tenant ID: " + TENANT_ID);
//        }
//
//        // Set up a tenant filter for runtime operations
//        runtimeService.createProcessInstanceBuilder()
//                .tenantId(TENANT_ID);
//    }
//}
//
package com.sigmap.sigmap_agpm.config;

import org.apache.commons.codec.digest.DigestUtils;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

@Configuration
public class TenantAwareFlowableConfig {

    private static final String TENANT_ID = "agpm";
    private static final String PROCESS_DEFINITION_LOCATION = "processes/";

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
            String bpmnXml = toString(resource.getInputStream());
            String newChecksum = DigestUtils.sha256Hex(bpmnXml);
            String processKey = fileName.replace(".bpmn20.xml", "");

            List<ProcessDefinition> existingDefs = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionTenantId(TENANT_ID)
                    .processDefinitionKey(processKey)
                    .orderByProcessDefinitionVersion().desc()
                    .list();

            boolean alreadyDeployed = existingDefs.stream().anyMatch(def -> {
                try (InputStream deployedStream = repositoryService.getResourceAsStream(def.getDeploymentId(), fileName)) {
                    String deployedXml = toString(deployedStream);
                    return newChecksum.equals(DigestUtils.sha256Hex(deployedXml));
                } catch (IOException e) {
                    return false;
                }
            });

            if (!alreadyDeployed) {
                repositoryService.createDeployment()
                        .tenantId(TENANT_ID)
                        .name("AGPM Process Deployment")
                        .addInputStream(fileName, resource.getInputStream())
                        .deploy();
                System.out.println(" Deployed: " + fileName);
            } else {
                System.out.println("Skipped (no changes): " + fileName);
            }
        }

        runtimeService.createProcessInstanceBuilder().tenantId(TENANT_ID);
    }

    private String toString(InputStream stream) {
        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }
}
