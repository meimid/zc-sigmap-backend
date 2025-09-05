package com.sigmap.common.ppms.exception;


import lombok.Data;

import java.util.List;
@Data
public class PpmNotAssociatedWithUserAcException extends RuntimeException {
  private final List<Long> ppmIds;
  private final List<Integer> versions;

  public PpmNotAssociatedWithUserAcException(String message, List<Long> ppmIds, List<Integer> versions) {
    super(message);
    this.ppmIds = ppmIds;
    this.versions = versions;
  }
}
