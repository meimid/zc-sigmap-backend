package com.sigmap.common.ref_params.exceptions;

import lombok.Data;

@Data
public class RefParamHasAssociatedCom extends RuntimeException {
  private final Integer id;
  public RefParamHasAssociatedCom(String message, Integer id) {
    super(message);
    this.id = id;
  }
}
