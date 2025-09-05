package com.sigmap.common.monnaie.response;

import lombok.Data;

/** A Response object for the {@link } entity. */
@Data
public class MonnaieResponseDto {
  private Integer id;

  private String code;

  private String description;

  private Double taux;
}
