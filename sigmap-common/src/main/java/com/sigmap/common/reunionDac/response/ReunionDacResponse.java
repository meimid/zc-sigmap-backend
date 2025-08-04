package com.sigmap.common.reunionDac.response;

import com.sigmap.common.dac_docs.response.DacDocsResponse;
import lombok.Data;

@Data
public class ReunionDacResponse {
    private DacDocsResponse dac;
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaFieldsId;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;
    private String fieldValue;
    private String sourceId;
    private String processInstanceId;
    private String taskId;

}
