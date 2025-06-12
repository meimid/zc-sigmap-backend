package com.sigmap.common.agpm.exceptions;

import com.sigmap.common.entity.ProcessStatus;
import lombok.Data;
import java.util.List;

@Data
public class AgpmInvalidStatusException extends Exception {
  private final List<Integer> agpmIds;
  private final List<Integer> agpmVersions;
  private final ProcessStatus currentStatus;

  public AgpmInvalidStatusException(String message, List<Integer> agpmIds, List<Integer> agpmVersions, ProcessStatus currentStatus) {
    super(message);
    this.agpmIds = agpmIds;
    this.agpmVersions = agpmVersions;
    this.currentStatus = currentStatus;
  }
}