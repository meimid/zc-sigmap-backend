package com.sigmap.common.dac_docs.request;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DacDocsRequest {
    private Integer dacId;
    private Integer dynaFieldsId;
    private Integer dynaDocsId;
    private String fieldValue;
    private String sourceId;
    private Integer docId;
    private List<DacDocsDetail> details = new ArrayList<>();

    @Data
    public static class DacDocsDetail {
        private Integer dynaFieldsDetailsId;
        private String sourceId;
        private String fieldValue;
        private Integer rowId;

    }
}
