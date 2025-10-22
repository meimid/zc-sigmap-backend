package com.sigmap.common.dyna_docs.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sigmap.common.dyna_docs_model.DynaDocsModel;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
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

  private ModeDePassationSelection modePassation;

  private Integer dynaDocOrder;

  private ActiveStatus status;

  private Boolean isStatic;

  private Boolean isAAO;

  private DynaDocsModel dynaDocsModel;

  @JsonInclude(Include.NON_NULL)
  private List<DynaFieldsResponse> fields;
}
