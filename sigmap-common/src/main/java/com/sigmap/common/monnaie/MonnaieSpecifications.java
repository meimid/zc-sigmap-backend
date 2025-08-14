package com.sigmap.common.monnaie;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

/** Specification for managing {@link Monnaie}. */
public class MonnaieSpecifications {
  /** search. */
  public static Specification<Monnaie> search(String search) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("code"), "%" + search + "%"),
          criteriaBuilder.like(root.get("description"), "%" + search + "%"),
          criteriaBuilder.like(root.get("taux"), "%" + search + "%"));
    };
  }

  /** id. */
  public static Specification<Monnaie> id(Integer id) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("id"), id);
    };
  }

  /** description. */
  public static Specification<Monnaie> description(String description) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("description"), "%" + description + "%");
    };
  }


  /** deleted. */
  public static Specification<Monnaie> deleted(boolean deleted) {
    return (root, query, criteriaBuilder) -> {
      if (!deleted) {
        return criteriaBuilder.or(
            criteriaBuilder.isNull(root.get("isDeleted")),
            criteriaBuilder.isFalse(root.get("isDeleted")));
      }
      return criteriaBuilder.isTrue(root.get("isDeleted"));
    };
  }

  /** code. */
  public static Specification<Monnaie> code(String code) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("code"), code );
    };
  }
  /** taux. */
  public static Specification<Monnaie> taux(Double taux) {
    return (Root<Monnaie> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
      if (taux == null) {
        return null;
      }
      return criteriaBuilder.equal(root.get("taux"), taux);
    };

  }
}
