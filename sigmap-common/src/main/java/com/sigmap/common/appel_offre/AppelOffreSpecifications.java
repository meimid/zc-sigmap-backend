package com.sigmap.common.appel_offre;

import com.sigmap.common.config.ModePassationType;
import com.sigmap.common.entity.StatusAppelOffre;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AppelOffreSpecifications {


    public static Specification<AppelOffre> search(String search) {
        return (root, query, cb) -> {
            String like = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.like(cb.lower(root.get("reference")), like));
            predicates.add(cb.like(cb.lower(root.get("nom")), like));
            predicates.add(cb.like(cb.lower(root.get("description")), like));
            predicates.add(cb.like(cb.lower(root.get("etape")), like));

            // enums
            try {
                ModePassationType mode = ModePassationType.valueOf(search);
                predicates.add(cb.equal(root.get("modePassation"), mode));
            } catch (Exception ignored) {}

            try {
                StatusAppelOffre st = StatusAppelOffre.valueOf(search);
                predicates.add(cb.equal(root.get("statusAppelOffre"), st));
            } catch (Exception ignored) {}

            // boolean
            if ("true".equalsIgnoreCase(search) || "false".equalsIgnoreCase(search)) {
                predicates.add(cb.equal(root.get("lotSepares"), Boolean.parseBoolean(search)));
            }

            // relation PPM
            try {
                Join<AppelOffre, Ppm> ppm = root.join("ppm", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(ppm.get("ref")), like));
                predicates.add(cb.like(cb.lower(ppm.get("nom")), like));
                predicates.add(cb.like(cb.lower(ppm.get("annee")), like));
            } catch (Exception ignored) {}

            // relation Realisation
            try {
                Join<AppelOffre, Realisation> real = root.join("realisation", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(real.get("realisationEnvisagees")), like));
                predicates.add(cb.like(cb.lower(real.get("beneficiaire")), like));
            } catch (Exception ignored) {}

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    // Champs simples
    public static Specification<AppelOffre> reference(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("reference")), "%" + value.toLowerCase() + "%");
    }

    public static Specification<AppelOffre> nom(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nom")), "%" + value.toLowerCase() + "%");
    }

    public static Specification<AppelOffre> description(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), "%" + value.toLowerCase() + "%");
    }

    public static Specification<AppelOffre> etape(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("etape")), "%" + value.toLowerCase() + "%");
    }

    // Enums
    public static Specification<AppelOffre> modePassation(ModePassationType mp) {
        return (root, query, cb) -> cb.equal(root.get("modePassation"), mp);
    }

    public static Specification<AppelOffre> statusAppelOffre(StatusAppelOffre st) {
        return (root, query, cb) -> cb.equal(root.get("statusAppelOffre"), st);
    }

    // Boolean
    public static Specification<AppelOffre> lotSepares(Boolean val) {
        return (root, query, cb) -> cb.equal(root.get("lotSepares"), val);
    }

    // Composite key ppm
    public static Specification<AppelOffre> ppmId(Integer id) {
        return (root, query, cb) -> {
            try {
                Join<AppelOffre, Ppm> ppm = root.join("ppm", JoinType.LEFT);
                return cb.equal(ppm.get("id"), id);
            } catch (Exception e) {
                return null;
            }
        };
    }

    public static Specification<AppelOffre> ppmVer(Integer ver) {
        return (root, query, cb) -> {
            try {
                Join<AppelOffre, Ppm> ppm = root.join("ppm", JoinType.LEFT);
                return cb.equal(ppm.get("version"), ver);
            } catch (Exception e) {
                return null;
            }
        };
    }

    // Composite key realisation
    public static Specification<AppelOffre> realisationId(Integer id) {
        return (root, query, cb) -> {
            try {
                Join<AppelOffre, Realisation> real = root.join("realisation", JoinType.LEFT);
                return cb.equal(real.get("id"), id);
            } catch (Exception e) {
                return null;
            }
        };
    }

    public static Specification<AppelOffre> realisationVer(Integer ver) {
        return (root, query, cb) -> {
            try {
                Join<AppelOffre, Realisation> real = root.join("realisation", JoinType.LEFT);
                return cb.equal(real.get("version"), ver);
            } catch (Exception e) {
                return null;
            }
        };
    }

    public static Specification<AppelOffre> deleted(boolean deleted) {
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
}
