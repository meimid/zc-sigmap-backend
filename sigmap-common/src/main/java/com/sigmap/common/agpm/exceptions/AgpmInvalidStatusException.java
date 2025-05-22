package com.sigmap.common.agpm.exceptions;

import lombok.Data;
import java.util.List;

@Data
public class AgpmInvalidStatusException extends Exception {
  private final List<Integer> agpmIds;
  private final List<Integer> agpmVersions;
  private final Integer currentStatusId;

  public AgpmInvalidStatusException(String message, List<Integer> agpmIds, List<Integer> agpmVersions, Integer currentStatusId) {
    super(message);
    this.agpmIds = agpmIds;
    this.agpmVersions = agpmVersions;
    this.currentStatusId = currentStatusId;
  }
}