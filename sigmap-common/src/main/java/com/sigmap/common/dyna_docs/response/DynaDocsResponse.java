package com.sigmap.common.dyna_docs.response;

import com.sigmap.common.ref_params.RefParam;
import lombok.Data;

@Data
public class DynaDocsResponse {
  private Integer dynaDocsId;

  private Integer dynaDocsVer;

  private String name;

  private String templateFileName;

  private String guideFileName;

  private RefParam categorie;

  private Integer dynaDocOrder;

  private Integer status;
}
