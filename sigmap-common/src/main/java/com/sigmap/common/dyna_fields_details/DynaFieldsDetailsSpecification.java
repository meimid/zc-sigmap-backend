package com.sigmap.common.dyna_fields_details;

import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class DynaFieldsDetailsSpecification {
  public static Specification<DynaFieldsDetails> search(String search) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("sourceField"), "%" + search + "%"),
          criteriaBuilder.like(root.get("source"), "%" + search + "%"),
          criteriaBuilder.like(root.get("columnPos"), "%" + search + "%"),
          criteriaBuilder.like(root.get("columnName"), "%" + search + "%"));
    };
  }

  public static Specification<DynaFieldsDetails> detailId(Integer detailId) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("dynaFieldsDetailsId"), detailId);
    };
  }

  public static Specification<DynaFieldsDetails> dynaDocId(Integer dynaDocId) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("dynaDocsId"), dynaDocId);
    };
  }

  public static Specification<DynaFieldsDetails> dynaDocVer(Integer dynaDocVer) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("dynaDocsVer"), dynaDocVer);
    };
  }

  public static Specification<DynaFieldsDetails> dynaFieldsId(Integer dynaFieldsId) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("dynaFieldsId"), dynaFieldsId);
    };
  }

  public static Specification<DynaFieldsDetails> typeId(Integer typeId) {
    return (root, query, criteriaBuilder) -> {
      Join<DynaFieldsDetails, RefParam> join = root.join("typeId");
      return criteriaBuilder.equal(join.get("id"), typeId);
    };
  }

  public static Specification<DynaFieldsDetails> columnName(String columnName) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("columnName"), columnName);
    };
  }

  public static Specification<DynaFieldsDetails> columnPos(Integer columnPos) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("columnPos"), columnPos);
    };
  }

  public static Specification<DynaFieldsDetails> source(String source) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("source"), source);
    };
  }

  public static Specification<DynaFieldsDetails> sourceField(String sourceField) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("source_field"), sourceField);
    };
  }
}
