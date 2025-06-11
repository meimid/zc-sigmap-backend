package com.sigmap.common.ref_types.request;

import com.sigmap.common.config.RefTypes;
import lombok.Data;

/**
 * Create a RefType request.
 *
 * @see RefType
 */
@Data
public class RefTypeRequest {
  private Integer id;
  private String description;
  private RefTypes refCode;
  private Boolean systemType;
}
