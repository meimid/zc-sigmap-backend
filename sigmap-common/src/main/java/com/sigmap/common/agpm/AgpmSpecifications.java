package com.sigmap.common.agpm;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.CompositeId;
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

    public static Specification<Agpm> statusId(Integer statusId) {
        return (root, query, criteriaBuilder) -> {
            if (statusId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("statusId"), statusId);
        };
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
}