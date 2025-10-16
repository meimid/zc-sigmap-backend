package com.sigmap.common.dyna_fields.request;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class DynaFieldsQuery {
  private Boolean all;

  private String search;

  private Integer dynaDocId;

  private Integer dynaDocVer;

  private String name;

  private String ref;

  private String label;

  private String description;

  private Integer typeId;

  private String source;

  private String sourceField;
  private Integer dynaFieldsId;

  public Specification<DynaFields> toSpecification() {
    Specification<DynaFields> spec = Specification.where(null);

    if (search != null && !search.isBlank()) {
      spec = spec.and(DynaFieldsSpecifications.search(search));
    }

    if (dynaFieldsId != null) {
      spec = spec.and(DynaFieldsSpecifications.dynaFieldId(dynaFieldsId));
    }

    if (dynaDocId != null) {
      spec = spec.and(DynaFieldsSpecifications.dynaDocId(dynaDocId));
    }

    if (dynaDocVer != null) {
      spec = spec.and(DynaFieldsSpecifications.dynaDocVer(dynaDocVer));
    }

    if (name != null && !name.isBlank()) {
      spec = spec.and(DynaFieldsSpecifications.name(name));
    }

    if (ref != null && !ref.isBlank()) {
      spec = spec.and(DynaFieldsSpecifications.ref(ref));
    }

    if (label != null && !label.isBlank()) {
      spec = spec.and(DynaFieldsSpecifications.label(label));
    }

    if (description != null && !description.isBlank()) {
      spec = spec.and(DynaFieldsSpecifications.description(description));
    }

    if (typeId != null) {
      spec = spec.and(DynaFieldsSpecifications.typeId(typeId));
    }
    if (source != null && !source.isBlank()) {
      spec = spec.and(DynaFieldsSpecifications.source(source));
    }

    if (sourceField != null && !sourceField.isBlank()) {
      spec = spec.and(DynaFieldsSpecifications.sourceField(sourceField));
    }

    return spec;
  }
}
