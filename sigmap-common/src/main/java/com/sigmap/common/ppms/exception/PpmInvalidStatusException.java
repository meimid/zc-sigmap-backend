package com.sigmap.common.ppms.exception;

import com.sigmap.common.entity.ProcessStatus;
import lombok.Data;

import java.util.List;

@Data
public class PpmInvalidStatusException extends Exception {
  private final List<Integer> ppmIds;
  private final List<Integer> ppmVersions;
  private final ProcessStatus currentStatus;

  public PpmInvalidStatusException(String message,
                                   List<Integer> ppmIds,
                                   List<Integer> ppmVersions,
                                   ProcessStatus currentStatus) {
    super(message);
    this.ppmIds = ppmIds;
    this.ppmVersions = ppmVersions;
    this.currentStatus = currentStatus;
  }
}
