package com.sigmap.common.dyna_docs.request;

import com.sigmap.common.entity.ActiveStatus;
import lombok.Data;

@Data
public class DynaDocsRequest {
  private String name;

  private String templateFileName;

  private String guideFileName;

  private Integer typeMarcheId;

  private Integer dynaDocOrder;

  private ActiveStatus status = ActiveStatus.NON_ACTIF;

  private Boolean isStatic;
}
