package com.sigmap.common.dac_docs_details;

import org.springframework.data.jpa.domain.Specification;

public class DacDocsDetailsSpecification {

  /**
   * Creates a specification to filter DacDocsDetails by dynamic fields details ID.
   *
   * @param dynaFieldsDetailsId The dynamic fields details ID to filter by
   * @return A specification for filtering by dynamic fields details ID
   */
  public static Specification<DacDocsDetails> byDynaFieldsDetailsId(Integer dynaFieldsDetailsId) {
    return (root, query, criteriaBuilder) -> {
      if (dynaFieldsDetailsId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("dynaFieldsDetailsId"), dynaFieldsDetailsId);
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by row ID.
   *
   * @param rowId The row ID to filter by
   * @return A specification for filtering by row ID
   */
  public static Specification<DacDocsDetails> byRowId(Integer rowId) {
    return (root, query, criteriaBuilder) -> {
      if (rowId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("rowId"), rowId);
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by DAC ID.
   *
   * @param dacId The DAC ID to filter by
   * @return A specification for filtering by DAC ID
   */
  public static Specification<DacDocsDetails> byDacId(Integer dacId) {
    return (root, query, criteriaBuilder) -> {
      if (dacId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("dacId"), dacId);
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by DAC version.
   *
   * @param dacVer The DAC version to filter by
   * @return A specification for filtering by DAC version
   */
  public static Specification<DacDocsDetails> byDacVer(Integer dacVer) {
    return (root, query, criteriaBuilder) -> {
      if (dacVer == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("dacVer"), dacVer);
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by dynamic docs ID.
   *
   * @param dynaDocsId The dynamic docs ID to filter by
   * @return A specification for filtering by dynamic docs ID
   */
  public static Specification<DacDocsDetails> byDynaDocsId(Integer dynaDocsId) {
    return (root, query, criteriaBuilder) -> {
      if (dynaDocsId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("dynaDocsId"), dynaDocsId);
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by dynamic docs version.
   *
   * @param dynaDocsVer The dynamic docs version to filter by
   * @return A specification for filtering by dynamic docs version
   */
  public static Specification<DacDocsDetails> byDynaDocsVer(Integer dynaDocsVer) {
    return (root, query, criteriaBuilder) -> {
      if (dynaDocsVer == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("dynaDocsVer"), dynaDocsVer);
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by dynamic fields ID.
   *
   * @param dynaFieldsId The dynamic fields ID to filter by
   * @return A specification for filtering by dynamic fields ID
   */
  public static Specification<DacDocsDetails> byDynaFieldsId(Integer dynaFieldsId) {
    return (root, query, criteriaBuilder) -> {
      if (dynaFieldsId == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("dynaFieldsId"), dynaFieldsId);
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by source ID.
   *
   * @param sourceId The source ID to filter by
   * @return A specification for filtering by source ID
   */
  public static Specification<DacDocsDetails> bySourceId(String sourceId) {
    return (root, query, criteriaBuilder) -> {
      if (sourceId == null || sourceId.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceId")), "%" + sourceId.toLowerCase() + "%");
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by field value.
   *
   * @param fieldValue The field value to filter by
   * @return A specification for filtering by field value
   */
  public static Specification<DacDocsDetails> byFieldValue(String fieldValue) {
    return (root, query, criteriaBuilder) -> {
      if (fieldValue == null || fieldValue.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("fieldValue")), "%" + fieldValue.toLowerCase() + "%");
    };
  }

  /**
   * Creates a specification to filter DacDocsDetails by multiple criteria.
   *
   * @param dynaFieldsDetailsId The dynamic fields details ID to filter by
   * @param rowId The row ID to filter by
   * @param dacId The DAC ID to filter by
   * @param dacVer The DAC version to filter by
   * @param dynaDocsId The dynamic docs ID to filter by
   * @param dynaDocsVer The dynamic docs version to filter by
   * @param dynaFieldsId The dynamic fields ID to filter by
   * @param sourceId The source ID to filter by
   * @param fieldValue The field value to filter by
   * @return A specification for filtering by multiple criteria
   */
  public static Specification<DacDocsDetails> byMultipleCriteria(
      Integer dynaFieldsDetailsId,
      Integer rowId,
      Integer dacId,
      Integer dacVer,
      Integer dynaDocsId,
      Integer dynaDocsVer,
      Integer dynaFieldsId,
      String sourceId,
      String fieldValue) {
    return Specification.where(byDynaFieldsDetailsId(dynaFieldsDetailsId))
        .and(byRowId(rowId))
        .and(byDacId(dacId))
        .and(byDacVer(dacVer))
        .and(byDynaDocsId(dynaDocsId))
        .and(byDynaDocsVer(dynaDocsVer))
        .and(byDynaFieldsId(dynaFieldsId))
        .and(bySourceId(sourceId))
        .and(byFieldValue(fieldValue));
  }
} 