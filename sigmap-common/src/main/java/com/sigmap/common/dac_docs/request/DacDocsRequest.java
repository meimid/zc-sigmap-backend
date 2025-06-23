package com.sigmap.common.dac_docs.request;



import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DacDocsRequest {
    private Integer dacId;
    private Integer dynaFieldsId;
    private Integer dynaDocsId;
    private String fieldValue;
    private String sourceId;
    private List<DacDocsDetail> details = new ArrayList<>();

    @Data
    public static class DacDocsDetail {
        private String sourceId;
        private String fieldValue;
        private Integer rowId;

    }
}
