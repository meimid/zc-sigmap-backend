package com.sigmap.common.dac_dyna_form;

import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.type_dossier.TypeDossier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class DacDynaFormSpecifications {

    private DacDynaFormSpecifications() {}

    /**  Full-text search across DAC, DynaForm, and TypeDossier */
    public static Specification<DacDynaForm> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            Join<DacDynaForm, DynaForm> join = root.join("dynaForm", JoinType.LEFT);

            Join<DynaForm, TypeDossier> typeDossierJoin =
                    join.join("typeDossier", JoinType.LEFT);
            String likeSearch = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("dacId").as(String.class)), likeSearch),
                    cb.like(cb.lower(root.get("dacVer").as(String.class)), likeSearch),
                    cb.like(cb.lower(root.get("dynaFormId").as(String.class)), likeSearch),
                    cb.like(cb.lower(join.get("name").as(String.class)), likeSearch),
                    cb.like(cb.lower(typeDossierJoin.get("code")), likeSearch),
                    cb.like(cb.lower(typeDossierJoin.get("libelle")), likeSearch),
                    cb.like(cb.lower(typeDossierJoin.get("description")), likeSearch)
            );
        };
    }

    /**
     * Filter DynaForms unattached to a given DAC (excluding soft-deleted ones).
     * A DynaForm is considered unattached if:
     *  - It belongs to the given TypeDossier.
     *  - It is not soft-deleted.
     *  - It does not have a non-deleted record in dac_dyna_form for the DAC.
     */
    public static Specification<DynaForm> unattachedToDac(Integer dacId, Integer dacVer, Integer typeDossierId ) {
        return (root, query, cb) -> {
            if (typeDossierId == null ) {
                return cb.conjunction();
            }



            var typePredicate = cb.equal(
                    root.get("typeDossier").get("id"),
                    typeDossierId
            );

            var notDeleted = cb.or(cb.isNull(root.get("isDeleted")), cb.isFalse(root.get("isDeleted")));

            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(DacDynaForm.class);
            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(subRoot.get("dacId"), dacId),
                            cb.equal(subRoot.get("dacVer"), dacVer),
                            cb.equal(subRoot.get("dynaFormId"), root.get("id")),
                            cb.equal(subRoot.get("dynaFormVer"), root.get("version")),
                            cb.isFalse(subRoot.get("isDeleted"))
                    );

            var notExists = cb.not(cb.exists(subquery));

            return cb.and(typePredicate, notDeleted, notExists);
        };
    }

    /**  Filter by DAC ID */
    public static Specification<DacDynaForm> byDacId(Integer dacId) {
        return (root, query, cb) -> {
            if (dacId == null) return cb.conjunction();
            return cb.equal(root.get("dacId"), dacId);
        };
    }

    /**  Filter by DAC version */
    public static Specification<DacDynaForm> byDacVer(Integer dacVer) {
        return (root, query, cb) -> {
            if (dacVer == null) return cb.conjunction();
            return cb.equal(root.get("dacVer"), dacVer);
        };
    }

    /**  Filter by DynaForm ID */
    public static Specification<DacDynaForm> byDynaFormId(Integer formId) {
        return (root, query, cb) -> {
            if (formId == null) return cb.conjunction();
            return cb.equal(root.get("dynaFormId"), formId);
        };
    }

    /**  Filter by DynaForm version */
    public static Specification<DacDynaForm> byDynaFormVer(Integer formVer) {
        return (root, query, cb) -> {
            if (formVer == null) return cb.conjunction();
            return cb.equal(root.get("dynaFormVer"), formVer);
        };
    }

    /**  Filter by field ID (dyna_form_fields_id) */
    public static Specification<DacDynaForm> byDynaFormFieldsId(Integer fieldId) {
        return (root, query, cb) -> {
            if (fieldId == null) return cb.conjunction();
            return cb.equal(root.get("dynaFormFieldsId"), fieldId);
        };
    }

    /**  Filter by TypeDossier (ADM / TECH / FIN) */
    public static Specification<DacDynaForm> byTypeDossierCode(String typeDossierCode) {
        return (root, query, cb) -> {
            if (typeDossierCode == null) {
                return cb.conjunction();
            }

            Join<DacDynaForm, DynaForm> join = root.join("dynaForm", JoinType.LEFT);
            return cb.equal(
                    cb.upper(join.get("typeDossier").get("code")),
                    typeDossierCode.trim().toUpperCase()
            );        };
    }

    public static Specification<DacDynaForm> byTypeDossierId(String typeDossierId) {
        return (root, query, cb) -> {
            if (typeDossierId == null) {
                return cb.conjunction();
            }

            Join<DacDynaForm, DynaForm> join = root.join("dynaForm", JoinType.LEFT);
            return cb.equal(
                    cb.upper(join.get("typeDossier").get("id")),
                    typeDossierId.trim()
            );        };
    }

    /**  Filter by display (true / false) */
    public static Specification<DacDynaForm> byDisplay(Boolean display) {
        return (root, query, cb) -> {
            if (display == null) return cb.conjunction();
            return cb.equal(root.get("display"), display);
        };
    }
}
