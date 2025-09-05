package com.sigmap.common.ref_params.exceptions;

import lombok.Getter;

@Getter
public class HasAssociatedDynaField extends RuntimeException {
  private final Integer id;

  public HasAssociatedDynaField(Integer id) {
    super("RefParam " + id + " has associated DynaField");
    this.id = id;
  }

  public HasAssociatedDynaField(Integer id, String message) {
    super(message);
    this.id = id;
  }
}
