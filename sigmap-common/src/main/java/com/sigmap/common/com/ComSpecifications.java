package com.sigmap.common.com;

import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/** Specification for managing {@link Com}. */
public class ComSpecifications {
  /** search. */
  public static Specification<Com> search(String search) {
    return (root, query, criteriaBuilder) -> {
      Join<Com, RefParam> join = root.join("comType");

      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
          criteriaBuilder.like(root.get("decretCreation"), "%" + search + "%"),
          criteriaBuilder.like(root.get("responsable"), "%" + search + "%"),
          criteriaBuilder.like(root.get("ancrage"), "%" + search + "%"),
          criteriaBuilder.like(root.get("contact"), "%" + search + "%"),
          criteriaBuilder.like(root.get("adresse"), "%" + search + "%"),
          criteriaBuilder.like(join.get("description"), "%" + search + "%"),
          criteriaBuilder.like(join.get("codeColor"), "%" + search + "%"),
          criteriaBuilder.like(join.get("codeColor"), "%" + search + "%"));
    };
  }

  /** id. */
  public static Specification<Com> id(Integer id) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("id"), id);
    };
  }

  /** decretCreation. */
  public static Specification<Com> decretCreation(String decretCreation) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("decretCreation"), "%" + decretCreation + "%");
    };
  }

  /** decretCreation. */
  public static Specification<Com> nom(String nom) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
    };
  }

  /** responsable. */
  public static Specification<Com> responsable(String responsable) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("responsable"), "%" + responsable + "%");
    };
  }

  /** ancrage. */
  public static Specification<Com> ancrage(String ancrage) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("ancrage"), "%" + ancrage + "%");
    };
  }

  /** contact. */
  public static Specification<Com> contact(String contact) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("contact"), "%" + contact + "%");
    };
  }

  /** adresse. */
  public static Specification<Com> adresse(String adresse) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("adresse"), "%" + adresse + "%");
    };
  }

  /** deleted. */
  public static Specification<Com> deleted(boolean deleted) {
    return (root, query, criteriaBuilder) -> {
      if (!deleted) {
        return criteriaBuilder.or(
            criteriaBuilder.isNull(root.get("isDeleted")),
            criteriaBuilder.isFalse(root.get("isDeleted")));
      }
      return criteriaBuilder.isTrue(root.get("isDeleted"));
    };
  }

  public static Specification<Com> hasComTypeId(Integer comTypeId) {
    return (root, query, cb) -> cb.equal(root.get("comType").get("id"), comTypeId);
  }
}
