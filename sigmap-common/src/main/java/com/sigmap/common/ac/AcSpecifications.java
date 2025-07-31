package com.sigmap.common.ac;

import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.com.Com;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class AcSpecifications {
  public static Specification<Ac> search(String search) {

    return (root, query, criteriaBuilder) -> {
      Join<Ac, RefParam> joinPays = root.join("pays");
      Join<Ac, Adresse>joinAdresses = joinPays.join("adresse");

      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("ancrage"), "%" + search + "%"),
          criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
          criteriaBuilder.like(root.get("contact"), "%" + search + "%") ,
          criteriaBuilder.like(root.get("email"), "%" + search + "%") ,
          criteriaBuilder.like(joinPays.get("description"), "%" + search + "%"),
          criteriaBuilder.like(joinPays.get("codeColor"),"%" + search + "%"),
          criteriaBuilder.like(joinPays.get("code"),"%" + search + "%"),
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

  public static Specification<Ac> findByComId(Integer comId) {

    return (root, query, cb) -> cb.equal(root.get("com").get("id"), comId);
  }
  public static Specification<Ac> hasCom(Integer comId) {
    return (root, query, criteriaBuilder) -> {

      // 1. Create an explicit INNER JOIN from Ac to Com.
      // This ensures we only get Ac entities that have an associated Com.
      Join<Ac, Com> comJoin = root.join("com", JoinType.INNER);

      // 2. Create a predicate to match the Com ID on the joined table.
      Predicate comIdPredicate = criteriaBuilder.equal(comJoin.get("id"), comId);

      // 3. Create a predicate to ensure the joined Com entity is not deleted.
      Predicate comNotDeletedPredicate = criteriaBuilder.isFalse(comJoin.get("isDeleted"));

      // 4. Create a predicate to ensure the root Ac entity is not deleted.
      Predicate acNotDeletedPredicate = criteriaBuilder.isFalse(root.get("isDeleted"));

      // 5. Combine all conditions.
      return criteriaBuilder.and(comIdPredicate, comNotDeletedPredicate, acNotDeletedPredicate);
    };
  }


  /** isItTutel. */
  public static Specification<Ac> isItTutel(boolean istTutel) {
    return (root, query, criteriaBuilder) -> {
      if (!istTutel) {
        return criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("istTutel")),
                criteriaBuilder.isFalse(root.get("istTutel")));
      }
      return criteriaBuilder.isTrue(root.get("istTutel"));
    };
  }

  public static Specification<Ac> hasComNull() {
    return (root, query, criteriaBuilder) -> {
      // Condition 1: The 'com' field in the Ac entity must be null.
      Predicate comIsNull = criteriaBuilder.isNull(root.get("com"));

      // Condition 2: The Ac entity itself must not be soft-deleted.
      Predicate notDeleted = criteriaBuilder.isFalse(root.get("isDeleted"));

      // Combine both conditions with an AND clause.
      return criteriaBuilder.and(comIsNull, notDeleted);
    };
  }
}
