package com.sigmap.common.monnaie.request;

import com.sigmap.common.config.RefTypes;
import lombok.Data;

@Data
public class MonnaieRequest {
  private String code;

  private String description;

  private Double taux;
}
