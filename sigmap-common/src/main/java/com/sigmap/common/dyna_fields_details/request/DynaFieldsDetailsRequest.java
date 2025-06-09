package com.sigmap.common.dyna_fields_details.request;

import lombok.Data;

@Data
public class DynaFieldsDetailsRequest {
  private String columnName;

  private Integer columnPos;

  private Integer typeId;

  private String source;

  private String sourceField;
}
