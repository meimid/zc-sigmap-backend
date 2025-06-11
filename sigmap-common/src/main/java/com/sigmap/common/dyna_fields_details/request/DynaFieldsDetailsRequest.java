package com.sigmap.common.dyna_fields_details.request;

import com.sigmap.common.dyna_fields_details.DynaFieldDetailsType;

import lombok.Data;

@Data
public class DynaFieldsDetailsRequest {
  private String columnName;

  private Integer columnPos;

  private DynaFieldDetailsType type;

  private String source;

  private String sourceField;
}
