package com.sigmap.common.dyna_fields.request;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DynaFieldsRequest {
  private Integer dynaDocsId;

  private String name;

  private String ref;

  private String label;

  private String description;

  private Integer typeId;

  private String source;

  private String sourceField;

  private Integer dynaFieldsOrder;

  private Integer status = 0;

  private List<Detail> details = new ArrayList<>();

  @Data
  public static class Detail {
    private String columnName;

    private Integer columnPos;

    private Integer typeId;

    private String source;

    private String sourceField;
  }
}
