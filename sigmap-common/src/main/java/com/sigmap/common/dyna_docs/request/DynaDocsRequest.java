package com.sigmap.common.dyna_docs.request;

import lombok.Data;

@Data
public class DynaDocsRequest {
  private String name;

  private String templateFileName;

  private String guideFileName;

  private Integer categorieId;

  private Integer dynaDocOrder;

  private Integer status = 1;
}
