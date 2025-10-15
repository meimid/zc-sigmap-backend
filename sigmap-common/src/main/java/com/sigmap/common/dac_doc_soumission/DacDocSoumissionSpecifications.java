package com.sigmap.common.dac_doc_soumission;

import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;

public class DacDocSoumissionSpecifications {
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

    /**
     * Creates a specification to filter by a specific validation date.
     *
     * @param dateValidate The date to filter by
     * @return A specification for filtering by dateValidate
     */
    public static Specification<DacDocSoumission> byDateValidate(Calendar dateValidate) {
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
    public static Specification<DacDocSoumission> byDateValidateBetween(Calendar startDate, Calendar endDate) {
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
    public static Specification<DacDocSoumission> dateValidateBefore(Calendar dateBefore) {
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
    public static Specification<DacDocSoumission> dateValidateEquals(Calendar dateExact) {
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
    public static Specification<DacDocSoumission> dateValidateAfter(Calendar dateAfter) {
        return (root, query, cb) -> {
            if (dateAfter == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("dateValidate"), dateAfter);
        };
    }
}
