package com.sigmap.common.dyna_fields_details.response;

import com.sigmap.common.dyna_fields_details.DynaFieldDetailsType;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

@Data
public class DynaFieldsDetailsResponse {
  private Integer dynaFieldsDetailsId;

  private Integer dynaDocsId;

  private Integer dynaDocsVer;

  private Integer dynaFieldsId;

  private String columnName;

  private Integer columnPos;

  private DynaFieldDetailsType type;

  private String source;

  private String source_field;
}
