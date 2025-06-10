package com.sigmap.common.dyna_docs.request;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class DynaDocsQuery {
  private String search;

  private String name;

  private String templateFileName;

  private String guideFileName;

  private Integer status;

  private Integer categorieId;

  public Specification<DynaDocs> toSpecification() {
    Specification<DynaDocs> spec = Specification.where(null);

    if (search != null && !search.isBlank()) {
      spec = spec.and(DynaDocsSpecifications.search(search));
    }

    if (name != null && !name.isBlank()) {
      spec = spec.and(DynaDocsSpecifications.name(name));
    }

    if (templateFileName != null && !templateFileName.isBlank()) {
      spec = spec.and(DynaDocsSpecifications.templateFileName(templateFileName));
    }

    if (guideFileName != null && !guideFileName.isBlank()) {
      spec = spec.and(DynaDocsSpecifications.guideFileName(guideFileName));
    }

    if (categorieId != null) {
      spec = spec.and(DynaDocsSpecifications.categoryId(categorieId));
    }

    return spec;
  }
}
