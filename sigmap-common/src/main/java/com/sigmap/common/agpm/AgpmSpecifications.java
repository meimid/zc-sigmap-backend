package com.sigmap.common.agpm;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.ppms.Ppm;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class AgpmSpecifications {
    public static Specification<Agpm> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<Agpm, Ac> join = root.join("ac");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("status"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("annee"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("nom"), "%" + search + "%")
            );
        };
    }

    public static Specification<Agpm> id(CompositeId id) {
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

    public static Specification<Agpm> version(Integer version) {
        return (root, query, criteriaBuilder) -> {
            if (version == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("version"), version);
        };
    }

    public static Specification<Agpm> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            if (ref == null || ref.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("ref"), "%" + ref + "%");
        };
    }

    public static Specification<Agpm> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            if (nom == null || nom.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

    public static Specification<Agpm> contenu(String contenu) {
        return (root, query, criteriaBuilder) -> {
            if (contenu == null || contenu.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("contenu"), "%" + contenu + "%");
        };
    }

    public static Specification<Agpm> status(ProcessStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Agpm> deleted(boolean deleted) {
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

    public static Specification<Agpm> acNom(String acNom) {
        return (root, query, criteriaBuilder) -> {
            if (acNom == null || acNom.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<Agpm, Ac> join = root.join("ac");
            return criteriaBuilder.like(join.get("nom"), "%" + acNom + "%");
        };
    }

    public static Specification<Agpm> hasStatusId(Integer statusId) {
        return (root, query, cb) -> cb.equal(root.get("status").get("id"), statusId);
    }

    public static Specification<Agpm> statusNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("status").get("isDeleted"), false);
    }

    public static Specification<Agpm> agpmAc(Integer acId) {
        return (root, query, cb) -> cb.equal(root.get("ac").get("id"), acId);
    }

    public static Specification<Agpm> isLatestVersion() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Agpm> subRoot = subquery.from(Agpm.class);
            subquery.select(criteriaBuilder.max(subRoot.get("version")))
                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                            criteriaBuilder.equal(subRoot.get("isDeleted"), false));
            return criteriaBuilder.equal(root.get("version"), subquery);
        };
    }

    public static Specification<Agpm> isPublished() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), "PUBLIE");
    }

    /**
     * Creates a Specification to find entities that are not marked as deleted.
     *
     * @return A Specification for non-deleted entities.
     */
    public static Specification<Agpm> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
    }

    /**
     * Creates a Specification to filter by the associated 'ac' entity's ID.
     *
     * @param acId The ID of the 'ac' entity to filter by.
     * @return A Specification for the given acId.
     */
    public static Specification<Agpm> withAcId(Integer acId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("ac").get("id"), acId);
    }

    /**
     * Creates a Specification to filter by the year of the 'updatedAt' field.
     *
     * @param year The year to filter by.
     * @return A Specification for the given year.
     */
    public static Specification<Agpm> withYear(Integer year) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.function("YEAR", Integer.class, root.get("updatedAt")),
                        year
                );
    }

    /**
     * Filter by annee.
     */
    public static Specification<Agpm> annee(String annee) {
        return (root, query, criteriaBuilder) -> {
            if (annee == null || annee.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.equal(root.get("annee"), annee);
        };
    }

    /**
     * Filters AGPMs by the boolean 'actualise' flag.
     * If {@code actualise} is null, no predicate is applied.
     *
     * @param actualise whether the AGPM is marked as actualised
     * @return a Specification matching the given flag or a conjunction when null
     */
    public static Specification<Agpm> actualise(Boolean actualise) {
        return (root, query, cb) -> {
            if (actualise == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("actualise"), actualise);
        };
    }

    /**
     * Filters AGPMs by an exact 'nbrActualisation' value.
     * If {@code value} is null, no predicate is applied.
     *
     * @param value exact number of actualisations to match
     * @return a Specification matching the given value or a conjunction when null
     */
    public static Specification<Agpm> nbrActualisation(Integer value) {
        return (root, query, cb) -> {
            if (value == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("nbrActualisation"), value);
        };
    }

    /**
     * Filters AGPMs by a range of 'nbrActualisation' values (inclusive).
     * Any null bound is ignored (i.e., treated as unbounded on that side).
     *
     * @param min minimum value inclusive (nullable)
     * @param max maximum value inclusive (nullable)
     * @return a Specification matching the given range or a conjunction when both bounds are null
     */
    public static Specification<Agpm> nbrActualisationBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            }
            if (min != null && max != null) {
                return cb.between(root.get("nbrActualisation"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("nbrActualisation"), min);
            }
            return cb.lessThanOrEqualTo(root.get("nbrActualisation"), max);
        };
    }

    public static Specification<Agpm> isLatestPublishedVersion() {
        return (root, query, cb) -> {
            // subquery to get MAX(version) among PUBLISHED, not deleted Agpms with same id
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Agpm> subRoot = subquery.from(Agpm.class);

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

    public static Specification<Agpm> agpmId(Integer id) {
        return (root, query, cb) -> {
            if (id == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("id"), id);
        };
    }

    public static Specification<Agpm> docId(Integer docId) {
        return (root, query, cb) -> {
            query.distinct(true);

            Join<Agpm, Docs> docsJoin = root.join("docs");
            return cb.equal(docsJoin.get("id"), docId);
        };
    }
}