package com.sigmap.common.dyna_form_fields;

import com.sigmap.common.entity.ActiveStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class DynaFormFieldSpecifications {
    private DynaFormFieldSpecifications() {}

    /** Only rows that are not soft-deleted */
    public static Specification<DynaFormField> notDeleted() {
        return (root, q, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    /** Of a given form (id + version) */
    public static Specification<DynaFormField> ofFormVersion(Integer formId, Integer formVer) {
        return (root, q, cb) -> cb.and(
                cb.equal(root.get("dynaFormId"), formId),
                cb.equal(root.get("dynaFormVer"), formVer)
        );
    }

    public static Specification<DynaFormField> fieldIdEq(Integer fieldId) {
        return (root, q, cb) -> cb.equal(root.get("dynaFormFieldsId"), fieldId);
    }

    public static Specification<DynaFormField> nameEq(String name) {
        return (root, q, cb) -> cb.equal(root.get("name"), name);
    }

    public static Specification<DynaFormField> labelEq(String label) {
        return (root, q, cb) -> cb.equal(root.get("label"), label);
    }

    public static Specification<DynaFormField> descriptionEq(String description) {
        return (root, q, cb) -> cb.equal(root.get("description"), description);
    }

    public static Specification<DynaFormField> typeIdEq(Integer typeId) {
        return (root, q, cb) -> cb.equal(root.get("typeId"), typeId);
    }

    public static Specification<DynaFormField> requiredEq(Boolean isRequired) {
        return (root, q, cb) -> cb.equal(root.get("isRequired"), isRequired);
    }

    public static Specification<DynaFormField> orderEq(Integer order) {
        return (root, q, cb) -> cb.equal(root.get("order"), order);
    }

    public static Specification<DynaFormField> statusEq(ActiveStatus status) {
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    /** Text search over common columns */
    public static Specification<DynaFormField> search(String search) {
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")),        like(search)),
                cb.like(cb.lower(root.get("label")),       like(search)),
                cb.like(cb.lower(root.get("description")), like(search))
        );
    }

    /**
     * If you ever store fields across multiple versions *with identical ids* and you need
     * “latest field rows for a given business (field) id across versions of the same form”,
     * you can use this subquery pattern.
     */
    public static Specification<DynaFormField> latestFieldRowPerFieldId(Integer formId) {
        return (root, query, cb) -> {
            query.distinct(true);

            Subquery<Integer> sq = query.subquery(Integer.class);
            Root<DynaFormField> sub = sq.from(DynaFormField.class);
            sq.select(cb.max(sub.get("dynaFormVer")))
                    .where(
                            cb.equal(sub.get("dynaFormId"), formId),
                            cb.equal(sub.get("dynaFormFieldsId"), root.get("dynaFormFieldsId")),
                            cb.isFalse(sub.get("isDeleted"))
                    );

            return cb.and(
                    cb.equal(root.get("dynaFormId"), formId),
                    cb.equal(root.get("dynaFormVer"), sq)
            );
        };
    }

    private static String like(String s) {
        return "%" + (s == null ? "" : s.toLowerCase().trim()) + "%";
    }
}
