package com.sigmap.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file.upload")
@Getter
@Setter
public class FileStorageProperties {
    private String parentPath;
    private String userProfilePath;
    private String acLogoPath;

}