package com.sigmap.common.ref_params.request;

import lombok.Data;

@Data
public class RefParamRequest {
  private String code;

  private String description;

  private Integer refTypeId;
}
