package com.sigmap.common.dyna_fields_details.request;

import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsSpecification;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class DynaFieldsDetailsQuery {
  private Integer dynaFieldsId;

  public Specification<DynaFieldsDetails> toSpecification() {
    Specification<DynaFieldsDetails> spec = Specification.where(null);

    if (dynaFieldsId != null) {
      spec = spec.and(DynaFieldsDetailsSpecification.dynaFieldsId(dynaFieldsId));
    }

    return spec;
  }
}
