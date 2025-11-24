package com.sigmap.common.dyna_docs.request;

import com.sigmap.common.entity.ActiveStatus;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaDocsRequest {
  private String name;

  private String templateFileName;

  private String guideFileName;

  private Integer typeMarcheId;

  private Integer modePassationId;

  private Integer dynaDocOrder;

  private ActiveStatus status = ActiveStatus.NON_ACTIF;

  private Boolean isStatic;

  private Boolean isAAO;

  private Integer dynaDocsModelId;
}
