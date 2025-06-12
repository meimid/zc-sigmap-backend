package com.sigmap.common.ref_types.exceptions;

import lombok.Getter;

@Getter
public class RefTypeCodeReserved extends RuntimeException {
  private final String code;

  public RefTypeCodeReserved(String code) {
    super("Code " + code + " is reserved");
    this.code = code;
  }
}
