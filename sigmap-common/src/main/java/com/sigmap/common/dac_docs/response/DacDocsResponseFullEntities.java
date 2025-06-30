package com.sigmap.common.dac_docs.response;


import com.sigmap.common.dyna_fields.DynaFields;

import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import lombok.Data;



@Data
public class DacDocsResponseFullEntities {
    private Integer dacId;
    private Integer dacVer;
    private String sourceId;
    private String fieldValue;
    private DynaFieldsDTO dynaField;
}
