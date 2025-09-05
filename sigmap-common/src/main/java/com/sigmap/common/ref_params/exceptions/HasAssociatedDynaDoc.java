package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedDynaDoc extends RuntimeException {
  private final Integer id;

  public HasAssociatedDynaDoc(Integer id) {
    super("RefParam " + id + " has associated DynaDoc");
    this.id = id;
  }

  public HasAssociatedDynaDoc(Integer id, String message) {
    super(message);
    this.id = id;
  }
}
