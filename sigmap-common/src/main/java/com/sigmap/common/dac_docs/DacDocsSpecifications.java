package com.sigmap.common.dac_docs;

import org.springframework.data.jpa.domain.Specification;

public class DacDocsSpecifications {

  /**
   * Creates a specification to filter DacDocs by DAC ID.
   *
   * @param dacId The DAC ID to filter by
   * @return A specification for filtering by DAC ID
   */
  public static Specification<DacDocs> byDacId(Integer dacId) {
    return (root, query, criteriaBuilder) -> {
      if (dacId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("id").get("dacId"), dacId);
    };
  }

  /**
   * Creates a specification to filter DacDocs by DAC version.
   *
   * @param dacVer The DAC version to filter by
   * @return A specification for filtering by DAC version
   */
  public static Specification<DacDocs> byDacVer(Integer dacVer) {
    return (root, query, criteriaBuilder) -> {
      if (dacVer == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("id").get("dacVer"), dacVer);
    };
  }

  /**
   * Creates a specification to filter DacDocs by dynamic docs ID.
   *
   * @param dynaDocsId The dynamic docs ID to filter by
   * @return A specification for filtering by dynamic docs ID
   */
  public static Specification<DacDocs> byDynaDocsId(Integer dynaDocsId) {
    return (root, query, criteriaBuilder) -> {
      if (dynaDocsId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("id").get("dynaDocsId"), dynaDocsId);
    };
  }

  /**
   * Creates a specification to filter DacDocs by dynamic docs version.
   *
   * @param dynaDocsVer The dynamic docs version to filter by
   * @return A specification for filtering by dynamic docs version
   */
  public static Specification<DacDocs> byDynaDocsVer(Integer dynaDocsVer) {
    return (root, query, criteriaBuilder) -> {
      if (dynaDocsVer == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("id").get("dynaDocsVer"), dynaDocsVer);
    };
  }

  /**
   * Creates a specification to filter DacDocs by dynamic fields ID.
   *
   * @param dynaFieldsId The dynamic fields ID to filter by
   * @return A specification for filtering by dynamic fields ID
   */
  public static Specification<DacDocs> byDynaFieldsId(Integer dynaFieldsId) {
    return (root, query, criteriaBuilder) -> {
      if (dynaFieldsId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("id").get("dynaFieldsId"), dynaFieldsId);
    };
  }

  /**
   * Creates a specification to filter DacDocs by source ID.
   *
   * @param sourceId The source ID to filter by
   * @return A specification for filtering by source ID
   */
  public static Specification<DacDocs> bySourceId(String sourceId) {
    return (root, query, criteriaBuilder) -> {
      if (sourceId == null || sourceId.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceId")), "%" + sourceId.toLowerCase() + "%");
    };
  }

  /**
   * Creates a specification to filter DacDocs by field value.
   *
   * @param fieldValue The field value to filter by
   * @return A specification for filtering by field value
   */
  public static Specification<DacDocs> byFieldValue(String fieldValue) {
    return (root, query, criteriaBuilder) -> {
      if (fieldValue == null || fieldValue.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("fieldValue")), "%" + fieldValue.toLowerCase() + "%");
    };
  }

  /**
   * Creates a specification to filter DacDocs by multiple criteria.
   *
   * @param dacId The DAC ID to filter by
   * @param dacVer The DAC version to filter by
   * @param dynaDocsId The dynamic docs ID to filter by
   * @param dynaDocsVer The dynamic docs version to filter by
   * @param dynaFieldsId The dynamic fields ID to filter by
   * @param sourceId The source ID to filter by
   * @param fieldValue The field value to filter by
   * @return A specification for filtering by multiple criteria
   */
  public static Specification<DacDocs> byMultipleCriteria(
      Integer dacId,
      Integer dacVer,
      Integer dynaDocsId,
      Integer dynaDocsVer,
      Integer dynaFieldsId,
      String sourceId,
      String fieldValue) {
    return Specification.where(byDacId(dacId))
        .and(byDacVer(dacVer))
        .and(byDynaDocsId(dynaDocsId))
        .and(byDynaDocsVer(dynaDocsVer))
        .and(byDynaFieldsId(dynaFieldsId))
        .and(bySourceId(sourceId))
        .and(byFieldValue(fieldValue));
  }
} 