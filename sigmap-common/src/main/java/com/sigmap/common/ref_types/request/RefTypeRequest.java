package com.sigmap.common.ref_types.request;

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
}
