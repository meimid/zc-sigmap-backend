package com.sigmap.common.docs.Response;

import lombok.Data;

@Data
public class DocsResponseDTO {
    private Integer id;
    private String name;
    private String alfresco_id;
    private String pathFolder;
    private String fileSize;
    private String alfrescoName;
    private String contentType;
    private String fileExtension;
}
