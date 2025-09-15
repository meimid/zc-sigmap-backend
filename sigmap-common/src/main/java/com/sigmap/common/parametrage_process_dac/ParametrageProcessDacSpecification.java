package com.sigmap.common.parametrage_process_dac;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ParametrageProcessDacSpecification {

    public static Specification<ParametrageProcessDac> findById(Integer acId, Integer typeMarcherId, Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (typeMarcherId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id").get("typeMarcher").get("id"), typeMarcherId));
            }

            if (modePassationId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id").get("modePassation").get("id"), modePassationId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    public static Specification<ParametrageProcessDac> typeMarcherId(Integer typeMarcherId) {
        return (root, query, criteriaBuilder) -> {
            if (typeMarcherId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("typeMarcher").get("id"), typeMarcherId);
        };
    }

    public static Specification<ParametrageProcessDac> modePassationId(Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            if (modePassationId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("modePassation").get("id"), modePassationId);
        };
    }

    public static Specification<ParametrageProcessDac> processExact(String process) {
        return (root, query, criteriaBuilder) -> {
            if (process == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("process"), process);
        };
    }

    public static Specification<ParametrageProcessDac> processLike(String processLike) {
        return (root, query, criteriaBuilder) -> {
            if (processLike == null) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("process")), "%" + processLike.toLowerCase() + "%");
        };
    }
}
