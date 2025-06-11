package com.sigmap.common.ref_params.response;

import com.sigmap.common.ref_types.response.RefTypeResponseDto;
import lombok.Data;

/** A Response object for the {@link } entity. */
@Data
public class RefParamResponseDto {
  private Integer id;

  private String code;

  private String description;
  private  final String codeColor;
}
