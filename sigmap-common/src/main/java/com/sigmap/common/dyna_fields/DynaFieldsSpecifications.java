package com.sigmap.common.dyna_fields;

import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class DynaFieldsSpecifications {

  public static Specification<DynaFields> latestVersionPerId() {
    return (root, query, cb) -> {
      query.distinct(true);

      Subquery<Integer> subquery = query.subquery(Integer.class);
      Root<DynaFields> subRoot = subquery.from(DynaFields.class);
      subquery
          .select(cb.max(subRoot.get("dynaDocsVer")))
          .where(cb.equal(subRoot.get("dynaFieldsId"), root.get("dynaFieldsId")));

      return cb.equal(root.get("dynaDocsVer"), subquery);
    };
  }

  public static Specification<DynaFields> search(String search) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("sourceField"), "%" + search + "%"),
          criteriaBuilder.like(root.get("source"), "%" + search + "%"),
          criteriaBuilder.like(root.get("ref"), "%" + search + "%"),
          criteriaBuilder.like(root.get("label"), "%" + search + "%"),
          criteriaBuilder.like(root.get("name"), "%" + search + "%"));
    };
  }

  public static Specification<DynaFields> dynaFieldId(Integer dynaFieldId) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("dynaFieldsId"), dynaFieldId);
    };
  }

  public static Specification<DynaFields> dynaDocId(Integer dynaDocId) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("dynaDocsId"), dynaDocId);
    };
  }

  public static Specification<DynaFields> dynaDocVer(Integer dynaDocVer) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("dynaDocsVer"), dynaDocVer);
    };
  }

  public static Specification<DynaFields> typeId(Integer typeId) {
    return (root, query, criteriaBuilder) -> {
      Join<DynaFields, RefParam> join = root.join("type_id");
      return criteriaBuilder.equal(join.get("id"), typeId);
    };
  }

  public static Specification<DynaFields> name(String name) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("name"), name);
    };
  }

  public static Specification<DynaFields> label(String label) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("label"), label);
    };
  }

  public static Specification<DynaFields> ref(String ref) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("ref"), ref);
    };
  }

  public static Specification<DynaFields> description(String description) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("description"), description);
    };
  }

  public static Specification<DynaFields> source(String source) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("source"), source);
    };
  }

  public static Specification<DynaFields> sourceField(String sourceField) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("sourceField"), sourceField);
    };
  }
}
