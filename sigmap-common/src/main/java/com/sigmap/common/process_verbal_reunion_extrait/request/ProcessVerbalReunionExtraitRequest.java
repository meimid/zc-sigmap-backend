package com.sigmap.common.process_verbal_reunion_extrait.request;

import com.sigmap.common.entity.ExtraitType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProcessVerbalReunionExtraitRequest {
    private Integer pvId;
    private Integer acId;
    private Integer ppmId;
    private Integer ppmVer;
    private MultipartFile file;
}
