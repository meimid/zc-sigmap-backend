package com.sigmap.common.dyna_docs.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.ref_params.RefParam;
import java.util.List;
import lombok.Data;

@Data
public class DynaDocsResponse {
  private Integer id;

  private Integer version;

  private String name;

  private String templateFileName;

  private String guideFileName;

  private RefParam typeMarche;

  private Integer dynaDocOrder;

  private ActiveStatus status;

  private Boolean isStatic;

  @JsonInclude(Include.NON_NULL)
  private List<DynaFieldsResponse> fields;
}
