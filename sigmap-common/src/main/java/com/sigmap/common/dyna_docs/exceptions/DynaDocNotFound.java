package com.sigmap.common.dyna_docs.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DynaDocNotFound extends RuntimeException {
  private final List<Integer> id;
  private final List<Integer> version;

  public DynaDocNotFound(Integer id) {
    super("DynaDocs with id " + id + " not found");
    this.id = List.of(id);
    this.version = List.of();
  }

  public DynaDocNotFound(String message, List<Integer> id, List<Integer> version) {
    super(message);
    this.id = id;
    this.version = version;
  }
}
