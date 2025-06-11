package com.sigmap.common.dyna_fields.exceptions;

public class DynaFieldNotFound extends RuntimeException {
  private Integer id;

  public DynaFieldNotFound(Integer id) {
    super("DynaField with id " + id + " not found");
    this.id = id;
  }

  public Integer getId() {
    return id;
  }
}
