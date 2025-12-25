package com.sigmap.common.dac_doc_soumission;

import com.sigmap.common.doc_soumission.DocSoumission;
import com.sigmap.common.type_dossier.TypeDossier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class DacDocSoumissionSpecifications {
    /** search */
    public static Specification<DacDocSoumission> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<DacDocSoumission, DocSoumission> docJoin =
                    root.join("docSoumission", JoinType.LEFT);

            Join<DocSoumission, TypeDossier> typeJoin =
                    docJoin.join("typeDossier", JoinType.LEFT);
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("dacId").as(String.class), "%" + search + "%"),
                    criteriaBuilder.like(root.get("dacVer").as(String.class), "%" + search + "%"),
                    criteriaBuilder.like(root.get("dateValidate").as(String.class), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(docJoin.get("nom")), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("code")), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("libelle")), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("description")), "%" + search + "%")
            );
        };
    }
    /**
     * Creates a specification to filter DacDocSoumission by DAC ID.
     *
     * @param dacId The DAC ID to filter by
     * @return A specification for filtering by DAC ID
     */
    public static Specification<DacDocSoumission> byDacId(Integer dacId) {
        return (root, query, criteriaBuilder) -> {
            if (dacId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("dacId"), dacId);
        };
    }

    /**
     * Creates a specification to filter DacDocSoumission by DAC version.
     *
     * @param dacVer The DAC version to filter by
     * @return A specification for filtering by DAC version
     */
    public static Specification<DacDocSoumission> byDacVer(Integer dacVer) {
        return (root, query, criteriaBuilder) -> {
            if (dacVer == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("dacVer"), dacVer);
        };
    }
    /**
     * Creates a specification to filter DacDocSoumission by Doc Soumission ID.
     *
     * @param docSoumissionId The DAC version to filter by
     * @return A specification for filtering by DAC version
     */
    public static Specification<DacDocSoumission> byDocSoumissionId(Integer docSoumissionId) {
        return (root, query, criteriaBuilder) -> {
            if (docSoumissionId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("docSoumissionId"), docSoumissionId);
        };
    }

    public static Specification<DacDocSoumission> docSoumissionNom(String nom) {
        return (root, query, cb) -> {
            Join<DacDocSoumission, DocSoumission> join = root.join("docSoumission");
            return cb.like(join.get("nom"), "%" + nom + "%");
        };
    }

    /** filter by docSoumission.typeDossier */

    public static Specification<DacDocSoumission> typeDossierCode(String typeDossier) {
        return (root, query, cb) -> {

            if (typeDossier == null || typeDossier.isBlank()) {
                return cb.conjunction();
            }

            // LEFT JOIN dac_doc_soumission -> doc_soumission
            Join<DacDocSoumission, DocSoumission> docJoin =
                    root.join("docSoumission", JoinType.LEFT);

            // JOIN doc_soumission -> type_dossier
            Join<DocSoumission, TypeDossier> typeJoin =
                    docJoin.join("typeDossier", JoinType.LEFT);

            return cb.equal(
                    cb.upper(typeJoin.get("code")),
                    typeDossier.trim().toUpperCase()
            );
        };
    }





    /**
     * Creates a specification to filter by a specific validation date.
     *
     * @param dateValidate The date to filter by
     * @return A specification for filtering by dateValidate
     */
    public static Specification<DacDocSoumission> byDateValidate(Date dateValidate) {
        return (root, query, cb) -> {
            if (dateValidate == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("dateValidate"), dateValidate);
        };
    }

    /**
     * Creates a specification to filter by a range of validation dates.
     *
     * @param startDate The start of the date range
     * @param endDate   The end of the date range
     * @return A specification for filtering by date range
     */
    public static Specification<DacDocSoumission> byDateValidateBetween(Date startDate, Date endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return cb.conjunction();
            } else if (startDate != null && endDate != null) {
                return cb.between(root.get("dateValidate"), startDate, endDate);
            } else if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("dateValidate"), startDate);
            } else {
                return cb.lessThanOrEqualTo(root.get("dateValidate"), endDate);
            }
        };
    }

    /**
     * Filters DacDocSoumission where dateValidate is before the given date.
     */
    public static Specification<DacDocSoumission> dateValidateBefore(Date dateBefore) {
        return (root, query, cb) -> {
            if (dateBefore == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("dateValidate"), dateBefore);
        };
    }

    /**
     * Filters DacDocSoumission where dateValidate is exactly equal to the given date.
     */
    public static Specification<DacDocSoumission> dateValidateEquals(Date dateExact) {
        return (root, query, cb) -> {
            if (dateExact == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("dateValidate"), dateExact);
        };
    }

    /**
     * Filters DacDocSoumission where dateValidate is after the given date.
     */
    public static Specification<DacDocSoumission> dateValidateAfter(Date dateAfter) {
        return (root, query, cb) -> {
            if (dateAfter == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("dateValidate"), dateAfter);
        };
    }

    public static Specification<DacDocSoumission> hasDacDocSoumission(Integer dacDocSoumissionId) {
        return (root, query, cb) -> cb.equal(root.get("docSoumissionId"), dacDocSoumissionId);
    }

}
