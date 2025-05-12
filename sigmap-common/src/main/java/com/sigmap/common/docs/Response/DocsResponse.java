package com.sigmap.common.docs.Response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class DocsResponse {
    private Integer id;
    private String name;
    private String alfresco_id;
    private String pathFolder;
    private String fileSize;
    private String alfrescoName;
    private String contentType;
    private String fileExtension;
    private BaseEntityResponse metadata;

}