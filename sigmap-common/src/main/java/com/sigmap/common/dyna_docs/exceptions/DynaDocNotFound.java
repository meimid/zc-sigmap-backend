package com.sigmap.common.dyna_docs.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DynaDocNotFound extends RuntimeException {
  private final List<Integer> id;
  private final List<Integer> version;

  public DynaDocNotFound(String message, List<Integer> id, List<Integer> version) {
    super(message);
    this.id = id;
    this.version = version;
  }
}
