package com.sigmap.common.dac_docs.response.customResponceDTO;

import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

import java.util.List;

@Data
public class DynaDocsResponseDTO {
    private Integer id;
    private Integer version;
    private String name;
    private String templateFileName;
    private String guideFileName;
    private RefParam typeMarche;
    private Integer dynaDocOrder;
    private String status;
    private Boolean isStatic;
    private List<DynaFieldsDTO> dynaFields;
    private DocsResponseDTO docs;
}
