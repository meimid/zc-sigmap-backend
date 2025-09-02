package com.sigmap.common.Participant;

import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.paas.Paa;
import org.springframework.data.jpa.domain.Specification;

public class ParticipantSpecifications {

    /**
     * Creates a Specification for a general text-based search across multiple fields.
     *
     * @param search The search term to look for.
     * @return A Specification for the Participant entity.
     */
    public static Specification<Participant> search(String search) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                criteriaBuilder.like(root.get("prenom"), "%" + search + "%"),
                criteriaBuilder.like(root.get("organisme"), "%" + search + "%"),
                criteriaBuilder.like(root.get("fonction"), "%" + search + "%"),
                criteriaBuilder.like(root.get("mail"), "%" + search + "%")
        );
    }

    /**
     * Filters by the associated Reunion ID.
     */
    public static Specification<Participant> reunionId(Integer reunionId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("reunionId"), reunionId);
    }

    /**
     * Filters by the associated User ID.
     */
    public static Specification<Participant> userId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    /**
     * Filters by the ParticipantType enum.
     */
    public static Specification<Participant> type(ParticipantType type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
    }

    /**
     * Filters by the 'present' status.
     */
    public static Specification<Participant> present(boolean isPresent) {
        return (root, query, criteriaBuilder) -> {
            if (isPresent) {
                return criteriaBuilder.isTrue(root.get("present"));
            }
            return criteriaBuilder.isFalse(root.get("present"));
        };
    }

    /**
     * Filters for records that are either soft-deleted or not.
     */
    public static Specification<Participant> deleted(boolean deleted) {
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
