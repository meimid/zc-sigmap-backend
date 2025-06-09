package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedDynaFieldDetail extends RuntimeException {
  private final Integer id;

  public HasAssociatedDynaFieldDetail(Integer id) {
    super("RefParam " + id + " has associated DynaFieldDetail");
    this.id = id;
  }

  public HasAssociatedDynaFieldDetail(Integer id, String message) {
    super(message);
    this.id = id;
  }
}
