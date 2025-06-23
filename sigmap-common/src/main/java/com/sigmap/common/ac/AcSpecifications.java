package com.sigmap.common.ac;

import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class AcSpecifications {
  public static Specification<Ac> search(String search) {

    return (root, query, criteriaBuilder) -> {
      Join<Ac, RefParam> join = root.join("tutelId");

      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("ancrage"), "%" + search + "%"),
          criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
          criteriaBuilder.like(root.get("contact"), "%" + search + "%") ,
          criteriaBuilder.like(root.get("email"), "%" + search + "%") ,
          criteriaBuilder.like(join.get("description"), "%" + search + "%"),
          criteriaBuilder.like(join.get("codeColor"),"%" + search + "%"),
          criteriaBuilder.like(join.get("code"),"%" + search + "%"));
    };
  }

  /** id. */
  public static Specification<Ac> id(Integer id) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("id"), id);
    };
  }

  /** ancrage. */
  public static Specification<Ac> ancrage(String ancrage) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("ancrage"), "%" + ancrage + "%");
    };
  }

  /** nom. */
  public static Specification<Ac> nom(String nom) {

    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
            criteriaBuilder.lower(root.get("nom")),
            nom.toLowerCase()
    );
  }



  /** code. */
  public static Specification<Ac> contact(String contact) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("contact"), "%" + contact + "%");
    };
  }

  /** email. */
  public static Specification<Ac> email(String email) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.like(root.get("email"), "%" + email + "%");
    };
  }

  /** deleted. */
  public static Specification<Ac> deleted(boolean deleted) {
    return (root, query, criteriaBuilder) -> {
      if (!deleted) {
        return criteriaBuilder.or(
            criteriaBuilder.isNull(root.get("isDeleted")),
            criteriaBuilder.isFalse(root.get("isDeleted")));
      }
      return criteriaBuilder.isTrue(root.get("isDeleted"));
    };
  }

  public static Specification<Ac> hasTutleId(Integer id) {
    return (root, query, cb) -> cb.equal(root.get("tutel").get("id"), id);
  }
}
