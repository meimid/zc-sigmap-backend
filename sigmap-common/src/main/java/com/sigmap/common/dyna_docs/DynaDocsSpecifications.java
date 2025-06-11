package com.sigmap.common.dyna_docs;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class DynaDocsSpecifications {

  public static Specification<DynaDocs> latestVersionPerId() {
    return (root, query, cb) -> {
      query.distinct(true);

      Subquery<Integer> subquery = query.subquery(Integer.class);
      Root<DynaDocs> subRoot = subquery.from(DynaDocs.class);
      subquery
          .select(cb.max(subRoot.get("version")))
          .where(cb.equal(subRoot.get("id"), root.get("id")));

      return cb.equal(root.get("version"), subquery);
    };
  }

  public static Specification<DynaDocs> latestById(Integer dynaDocsId) {
    return (root, query, cb) -> {
      Subquery<Integer> subquery = query.subquery(Integer.class);
      Root<DynaDocs> subRoot = subquery.from(DynaDocs.class);
      subquery
          .select(cb.max(subRoot.get("version")))
          .where(cb.equal(subRoot.get("id"), dynaDocsId));

      return cb.and(
          cb.equal(root.get("id"), dynaDocsId),
          cb.equal(root.get("version"), subquery));
    };
  }

  public static Specification<DynaDocs> search(String search) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.or(
          criteriaBuilder.like(root.get("templateFileName"), "%" + search + "%"),
          criteriaBuilder.like(root.get("guideFileName"), "%" + search + "%"),
          criteriaBuilder.like(root.get("name"), "%" + search + "%"));
    };
  }

  public static Specification<DynaDocs> categoryId(Integer categorieId) {
    return (root, query, criteriaBuilder) -> {
      Join<DynaDocs, RefParam> join = root.join("categorieId");
      return criteriaBuilder.equal(join.get("id"), categorieId);
    };
  }

  public static Specification<DynaDocs> name(String name) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("name"), name);
    };
  }

  public static Specification<DynaDocs> templateFileName(String templateFileName) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("templateFileName"), templateFileName);
    };
  }

  public static Specification<DynaDocs> guideFileName(String guideFileName) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("guideFileName"), guideFileName);
    };
  }

  public static Specification<DynaDocs> status(ActiveStatus status) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("status"), status);
    };
  }
}
