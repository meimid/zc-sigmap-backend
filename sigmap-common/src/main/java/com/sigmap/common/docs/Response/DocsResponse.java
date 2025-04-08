package com.sigmap.common.docs.Response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class DocsResponse {
    private Integer id;
    private String name;
    private String alfresco_id;
    private BaseEntityResponse metadata;

}
