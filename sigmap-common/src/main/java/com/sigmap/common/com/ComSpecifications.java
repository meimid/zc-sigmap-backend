package com.sigmap.common.com;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/** Specification for managing {@link Com}. */
public class ComSpecifications {
  /** search. */
  public static Specification<Com> search(String search) {
    return (root, query, criteriaBuilder) -> {
      Join<Com, RefParam> joinParams = root.join("comType");
      Join<Com, Adresse> joinAdresses = joinParams.join("theAdresse");


      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
          criteriaBuilder.like(root.get("decretCreation"), "%" + search + "%"),
          criteriaBuilder.like(root.get("responsable"), "%" + search + "%"),
          criteriaBuilder.like(root.get("ancrage"), "%" + search + "%"),
          criteriaBuilder.like(root.get("contact"), "%" + search + "%"),
          criteriaBuilder.like(root.get("adresse"), "%" + search + "%"),
          criteriaBuilder.like(joinParams.get("description"), "%" + search + "%"),
          criteriaBuilder.like(joinParams.get("codeColor"), "%" + search + "%"),
          criteriaBuilder.like(joinParams.get("codeColor"), "%" + search + "%"),
              criteriaBuilder.like(joinAdresses.get("tel1"), "%" + search + "%"),
              criteriaBuilder.like(joinAdresses.get("tel2"),"%" + search + "%"),
              criteriaBuilder.like(joinAdresses.get("fax"),"%" + search + "%"),
              criteriaBuilder.like(joinAdresses.get("adresse"),"%" + search + "%"),
              criteriaBuilder.like(joinAdresses.get("compAdresse"),"%" + search + "%"),
              criteriaBuilder.like(joinAdresses.get("ville"),"%" + search + "%"),
              criteriaBuilder.like(joinAdresses.get("codePostal"),"%" + search + "%"));
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

  public static Specification<Com> hasTute(Integer tutelId) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("tutel").get("id"), tutelId);
    };
  }
  public static Specification<Com> findActiveComsWithActiveTutelAuthority() {
    return (root, query, criteriaBuilder) -> {
      // Create an explicit INNER JOIN from Com to its tutel (Ac).
      Join<Com, Ac> tutelJoin = root.join("tutel", JoinType.INNER);

      // Condition 1: The Com entity itself must not be deleted.
      Predicate comNotDeleted = criteriaBuilder.isFalse(root.get("isDeleted"));

      // Condition 2: The associated tutel (Ac) must not be deleted.
      Predicate tutelNotDeleted = criteriaBuilder.isFalse(tutelJoin.get("isDeleted"));

      // Condition 3: The associated tutel (Ac) must be a tutel authority.
      Predicate isTutelAuthority = criteriaBuilder.isTrue(tutelJoin.get("istTutel"));

      // Combine all conditions with AND.
      return criteriaBuilder.and(comNotDeleted, tutelNotDeleted, isTutelAuthority);
    };
  }

}
