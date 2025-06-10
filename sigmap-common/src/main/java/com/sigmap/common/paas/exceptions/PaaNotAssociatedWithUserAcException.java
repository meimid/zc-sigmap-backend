package com.sigmap.common.paas.exceptions;

import java.util.List;

public class PaaNotAssociatedWithUserAcException extends RuntimeException {
  public PaaNotAssociatedWithUserAcException(String message, List<Long> paaIds, List<Integer> paaVersions) {
    super(message);
  }
}