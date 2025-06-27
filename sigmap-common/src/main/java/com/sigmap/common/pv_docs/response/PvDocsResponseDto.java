package com.sigmap.common.pv_docs.response;

import com.sigmap.common.docs.Response.DocsResponseDTO;
import lombok.Data;

@Data
public class PvDocsResponseDto {
    private DocsResponseDTO doc;
    private boolean pv;
}
