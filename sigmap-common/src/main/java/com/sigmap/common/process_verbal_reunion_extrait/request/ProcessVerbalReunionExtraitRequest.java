package com.sigmap.common.process_verbal_reunion_extrait.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProcessVerbalReunionExtraitRequest {
    private Integer extraitId;

    private Integer pvId;

    private Integer acId;

    private MultipartFile file;
}
