package com.sigmap.common.dac_docs.request;

import com.sigmap.common.dac.request.DacEditRequest;
import lombok.Data;

import java.util.List;

@Data
public class DacDoccsAndDacRequest {
    private List<DacDocsRequest> requests;
    private DacEditRequest dacEditRequest;
}
