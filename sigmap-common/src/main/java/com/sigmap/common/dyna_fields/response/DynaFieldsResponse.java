package com.sigmap.common.dyna_fields.response;

import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.ref_params.RefParam;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DynaFieldsResponse {
  private Integer dynaFieldsId;

  private Integer dynaDocsId;

  private Integer dynaDocsVer;

  private String name;

  private String ref;

  private String label;

  private String description;

  private DynaFieldType type;

  private String source;

  private String sourceField;

  private Integer dynaFieldsOrder;

  private ActiveStatus status;

  private List<Detail> details = new ArrayList<>();

  @Data
  public static class Detail {
    private String columnName;

    private Integer columnPos;

    private RefParam typeId;

    private String source;

    private String sourceField;
  }
}
