package com.sigmap.common.process_verbal.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PvRequest {
    Integer ppmId;
    Integer ppmVersion;
    String taskId;
    String processInstanceId;
    MultipartFile document;
}
