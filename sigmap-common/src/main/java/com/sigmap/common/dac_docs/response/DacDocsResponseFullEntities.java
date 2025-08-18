package com.sigmap.common.dac_docs.response;


import com.sigmap.common.dac_docs_details.response.DacDocsDetailsResponse;
import com.sigmap.common.dyna_fields.DynaFields;

import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import lombok.Data;

import java.util.Set;


@Data
public class DacDocsResponseFullEntities {
    private Integer dacId;
    private Integer dacVer;
    private String sourceId;
    private String fieldValue;

    private Integer dynaFieldsId;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;

    private Set<DacDocsDetailsResponse> dacDocsDetails;

}
