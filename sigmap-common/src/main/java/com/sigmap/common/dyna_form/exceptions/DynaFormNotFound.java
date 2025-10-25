package com.sigmap.common.dyna_form.exceptions;

import lombok.Getter;
import java.util.List;

@Getter
public class DynaFormNotFound extends RuntimeException {

  private final List<Integer> id;
  private final List<Integer> version;

  public DynaFormNotFound(String message, List<Integer> id, List<Integer> version) {
    super(message);
    this.id = id;
    this.version = version;
  }
}
