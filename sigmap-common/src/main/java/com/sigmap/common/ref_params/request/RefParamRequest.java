package com.sigmap.common.ref_params.request;

import lombok.Data;

@Data
public class RefParamRequest {
  // this  id to use same dto for create and update
  private Integer id;

  private String code;

  private String description;

  private Integer refTypeId;
}
