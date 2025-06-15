package com.sigmap.common.dyna_fields.request;

import java.util.ArrayList;
import java.util.List;

import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields_details.DynaFieldDetailsType;
import com.sigmap.common.entity.ActiveStatus;
import lombok.Data;

@Data
public class DynaFieldsRequest {
  private Integer dynaDocsId;

  private String name;

  private String ref;

  private String label;

  private String description;

  private DynaFieldType type;

  private String source;

  private String sourceField;

  private Integer dynaFieldsOrder;

  private ActiveStatus status = ActiveStatus.NON_ACTIF;

  private List<Detail> details = new ArrayList<>();

  @Data
  public static class Detail {
    private String columnName;

    private Integer columnPos;

    private DynaFieldDetailsType type;

    private String source;

    private String sourceField;
  }
}
