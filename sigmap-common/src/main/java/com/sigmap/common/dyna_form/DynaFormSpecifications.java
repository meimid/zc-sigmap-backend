package com.sigmap.common.dyna_form;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.FormType;
import com.sigmap.common.entity.TypeDossier;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class DynaFormSpecifications {

    private DynaFormSpecifications() {}

    /** Only rows that are not soft-deleted */
    public static Specification<DynaForm> notDeleted() {
        return (root, q, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    /** Latest version per business id (id) */
    public static Specification<DynaForm> latestVersionPerId() {
        return (root, query, cb) -> {
            query.distinct(true);

            Subquery<Integer> sq = query.subquery(Integer.class);
            Root<DynaForm> sub = sq.from(DynaForm.class);
            sq.select(cb.max(sub.get("version")))
                    .where(
                            cb.equal(sub.get("id"), root.get("id")),
                            cb.isFalse(sub.get("isDeleted"))
                    );

            return cb.equal(root.get("version"), sq);
        };
    }

    public static Specification<DynaForm> idEq(Integer id) {
        return (root, q, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<DynaForm> versionEq(Integer version) {
        return (root, q, cb) -> cb.equal(root.get("version"), version);
    }

    public static Specification<DynaForm> statusEq(ActiveStatus status) {
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<DynaForm> typeDossierEq(TypeDossier type) {
        return (root, q, cb) -> cb.equal(root.get("typeDossier"), type);
    }

    public static Specification<DynaForm> formTypeEq(FormType formType) {
        return (root, q, cb) -> cb.equal(root.get("formType"), formType);
    }

    public static Specification<DynaForm> nameEq(String name) {
        return (root, q, cb) -> cb.equal(root.get("name"), name);
    }

    public static Specification<DynaForm> nameLike(String search) {
        return (root, q, cb) -> cb.like(cb.lower(root.get("name")), like(search));
    }

    public static Specification<DynaForm> orderEq(Integer order) {
        return (root, q, cb) -> cb.equal(root.get("order"), order);
    }

    /** Simple multi-field search (header) */
    public static Specification<DynaForm> search(String search) {
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like(search)),
                cb.like(cb.lower(root.get("status").as(String.class)), like(search)),
                cb.like(cb.lower(root.get("typeDossier").as(String.class)), like(search)),
                cb.like(cb.lower(root.get("formType").as(String.class)), like(search)) // ✅ added formType search too
        );
    }

    private static String like(String s) {
        return "%" + (s == null ? "" : s.toLowerCase().trim()) + "%";
    }
}
