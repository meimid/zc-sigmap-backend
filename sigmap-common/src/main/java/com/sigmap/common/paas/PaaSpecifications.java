package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class PaaSpecifications {
    /**
     * Search across ref, nom, and ac.nom fields.
     */
    public static Specification<Paa> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if search is null/empty
            }
            Join<Paa, Ac> join = root.join("ac");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("status"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("annee"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("nom"), "%" + search + "%") // Assumes Ac has 'nom'
            );
        };
    }

    public static Specification<Paa> paaAc(List<Integer> acIds) {
        return (root, query, cb) -> cb.in(root.get("ac").get("id")).value(acIds);
    }

    /**
     * Filter by paa_id.
     */
    public static Specification<Paa> id(CompositeId id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("id"), id.getId()),
                    criteriaBuilder.equal(root.get("version"), id.getVersion())
            );
        };
    }

    /**
     * Filter by paa_ver.
     */
    public static Specification<Paa> version(Integer version) {
        return (root, query, criteriaBuilder) -> {
            if (version == null) {
                return criteriaBuilder.conjunction(); // Return all if null
            }
            return criteriaBuilder.equal(root.get("version"), version);
        };
    }

    /**
     * Filter by ref.
     */
    public static Specification<Paa> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            if (ref == null || ref.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(root.get("ref"), "%" + ref + "%");
        };
    }

    /**
     * Filter by nom.
     */
    public static Specification<Paa> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            if (nom == null || nom.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

    /**
     * Filter by annee.
     */
    public static Specification<Paa> annee(String annee) {
        return (root, query, criteriaBuilder) -> {
            if (annee == null || annee.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.equal(root.get("annee"), annee);
        };
    }

    /**
     * Filter by budgetEstime (exact match).
     */
    public static Specification<Paa> budgetEstime(BigDecimal budgetEstime) {
        return (root, query, criteriaBuilder) -> {
            if (budgetEstime == null) {
                return criteriaBuilder.conjunction(); // Return all if null
            }
            return criteriaBuilder.equal(root.get("budgetEstime"), budgetEstime);
        };
    }

    /**
     * Filter by status.
     */
    public static Specification<Paa> status(ProcessStatus status) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by deletion status.
     */
    public static Specification<Paa> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted"))
                );
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }

    /**
     * Filter by ac.nom (Ac entity's name).
     */
    public static Specification<Paa> acNom(String acNom) {
        return (root, query, criteriaBuilder) -> {
            if (acNom == null || acNom.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            Join<Paa, Ac> join = root.join("ac");
            return criteriaBuilder.like(join.get("nom"), "%" + acNom + "%");
        };
    }

    public static Specification<Paa> hasModePassationId(Integer id) {
        return (root, query, cb) -> cb.equal(root.get("modePassation").get("id"), id);
    }

    public static Specification<Paa> modePassationNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("modePassation").get("isDeleted"), false);
    }

    public static Specification<Paa> isLatestVersion() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Paa> subRoot = subquery.from(Paa.class);
            subquery.select(criteriaBuilder.max(subRoot.get("version")))
                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                            criteriaBuilder.equal(subRoot.get("isDeleted"), false));
            return criteriaBuilder.equal(root.get("version"), subquery);
        };
    }
    public static Specification<Paa> paaAc(Integer acId) {
        return (root, query, cb) -> cb.equal(root.get("ac").get("id"), acId);
    }


    public static Specification<Paa> isPublished() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), "PUBLIE");
    }

    /**
     * Creates a Specification to find entities that are not marked as deleted.
     *
     * @return A Specification for non-deleted entities.
     */
    public static Specification<Paa> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
    }

    /**
     * Creates a Specification to filter by the associated 'ac' entity's ID.
     *
     * @param acId The ID of the 'ac' entity to filter by.
     * @return A Specification for the given acId.
     */
    public static Specification<Paa> withAcId(Integer acId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ac").get("id"), acId);
    }

    /**
     * Creates a Specification to filter by the year of the 'updatedAt' field.
     *
     * @param year The year to filter by.
     * @return A Specification for the given year.
     */
    public static Specification<Paa> withYear(Integer year) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.function("YEAR", Integer.class, root.get("updatedAt")),
                        year
                );
    }

    public static Specification<Paa> anneeIn(List<String> annees) {
        return (root, query, cb) -> root.get("annee").in(annees);
    }
    public static Specification<Paa> isLatestPublishedVersion() {
        return (root, query, cb) -> {
            // subquery to get MAX(version) among PUBLISHED, not deleted Paas with same id
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Paa> subRoot = subquery.from(Paa.class);

            subquery.select(cb.max(subRoot.get("version")))
                    .where(
                            cb.equal(subRoot.get("id"), root.get("id")),
                            cb.equal(subRoot.get("status"), ProcessStatus.PUBLIE),
                            cb.equal(subRoot.get("isDeleted"), false)
                    );

            return cb.and(
                    cb.equal(root.get("status"), ProcessStatus.PUBLIE),
                    cb.equal(root.get("isDeleted"), false),
                    cb.equal(root.get("version"), subquery)
            );
        };
    }

    public static Specification<Paa> paaId(Integer id) {
        return (root, query, cb) -> {
            if (id == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("id"), id);
        };
    }

    public static Specification<Paa> docId(Integer docId) {
        return (root, query, cb) -> {
            query.distinct(true);

            Join<Paa, Docs> docsJoin = root.join("docs");

            return cb.and(
                    cb.equal(docsJoin.get("id"), docId),
                    cb.isFalse(root.get("isDeleted"))
            );
        };
    }

}