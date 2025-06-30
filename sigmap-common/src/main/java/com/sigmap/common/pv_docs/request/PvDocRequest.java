package com.sigmap.common.pv_docs.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PvDocRequest {
    private Integer docId;
    private boolean pv;
    MultipartFile document;
}
