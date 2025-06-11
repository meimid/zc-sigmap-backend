package com.sigmap.common.ref_params;

import com.sigmap.common.config.RefTypes;
import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

/** Specification for managing {@link RefParam}. */
public class RefParamSpecifications {
  /** search. */
  public static Specification<RefParam> search(String search) {
    return (root, query, criteriaBuilder) -> {
      Join<RefParam, RefType> join = root.join("refType");
      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("code"), "%" + search + "%"),
          criteriaBuilder.like(root.get("description"), "%" + search + "%"),
          criteriaBuilder.like(join.get("description"), "%" + search + "%"),
          criteriaBuilder.like(root.get("codeColor"), "%" + search + "%"),
          criteriaBuilder.like(join.get("systemType"), "%" + search + "%"));
    };
  }

  /** id. */
  public static Specification<RefParam> id(Integer id) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("id"), id);
    };
  }

  /** ref type id. */
  public static Specification<RefParam> refTypeId(Integer refTypeId) {
    return (root, query, criteriaBuilder) -> {
      Join<RefParam, RefType> join = root.join("refType");
      return criteriaBuilder.equal(join.get("id"), refTypeId);
    };
  }

  /** description. */
  public static Specification<RefParam> description(String description) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("description"), "%" + description + "%");
    };
  }

  /** code. */
  public static Specification<RefParam> code(String code) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("code"), "%" + code + "%");
    };
  }

  /** deleted. */
  public static Specification<RefParam> deleted(boolean deleted) {
    return (root, query, criteriaBuilder) -> {
      if (!deleted) {
        return criteriaBuilder.or(
            criteriaBuilder.isNull(root.get("isDeleted")),
            criteriaBuilder.isFalse(root.get("isDeleted")));
      }
      return criteriaBuilder.isTrue(root.get("isDeleted"));
    };
  }

  /** code color */
  public static Specification<RefParam> codeColor(String codeColor) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("codeColor"), "%" + codeColor + "%");
    };
  }

  /** systemType. */
  public static Specification<RefParam> systemType(boolean deletedRefparam) {
    return (root, query, criteriaBuilder) -> {
      Join<RefParam, RefType> refTypeJoin = root.join("refType");
      Predicate refTypeNotDeleted =
          criteriaBuilder.or(
              criteriaBuilder.isNull(refTypeJoin.get("isDeleted")),
              criteriaBuilder.isFalse(refTypeJoin.get("isDeleted")));

      Predicate refTypeNotSystemType =
          criteriaBuilder.or(
              criteriaBuilder.isNull(refTypeJoin.get("systemType")),
              criteriaBuilder.isFalse(refTypeJoin.get("systemType")));
      Predicate criteriaForAssociatedRefType =
          criteriaBuilder.and(refTypeNotDeleted, refTypeNotSystemType);
      Specification<RefParam> refParamDeletedFilter = deleted(deletedRefparam);
      Predicate predicateForRefParamDeleted =
          refParamDeletedFilter.toPredicate(root, query, criteriaBuilder);
      return criteriaBuilder.and(criteriaForAssociatedRefType, predicateForRefParamDeleted);
    };
  }

  public static Specification<RefParam> hasRefTypeId(Integer refTypeId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("refType").get("id"), refTypeId);
  }
  public static Specification<RefParam> hasRefTypeCode(RefTypes refType) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("refType").get("refCode"), refType.name());
  }
}
