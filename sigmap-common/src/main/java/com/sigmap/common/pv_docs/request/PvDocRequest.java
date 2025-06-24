package com.sigmap.common.pv_docs.request;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PvDocRequest {
    private Integer ppmId;
    private Integer ppmVersion;
    private String processVerbalId;
    private Integer docId;
    private boolean pv;
    MultipartFile document;
}
