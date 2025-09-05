package com.sigmap.common.agpm.response;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.Map;

/**
 * DTO for minimal AGPM response, containing id, version, ref, nom, and metadata.
 */
@Data
public class AgpmMinimalResponseDto {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String modelAlfrescoId;
    private ProcessStatus status;
    private BaseEntityResponse metadata;
}