package com.sigmap.common.ref_params.request;

import com.sigmap.common.config.RefTypes;
import lombok.Data;

@Data
public class RefParamRequest {
  private String code;

  private String description;

  private RefTypes refType;

  private String codeColor;
}
