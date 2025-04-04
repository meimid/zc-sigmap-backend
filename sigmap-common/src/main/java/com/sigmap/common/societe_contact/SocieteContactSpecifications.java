package com.sigmap.common.societe_contact;


import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class SocieteContactSpecifications {


    /** ids */
    public static Specification<SocieteContact> Ids(Integer societeId, Integer contactId) {
        return (root, query, criteriaBuilder) -> {
            Predicate societePredicate = criteriaBuilder.equal(root.get("societeContactId").get("societe").get("id"), societeId);
            Predicate contactPredicate = criteriaBuilder.equal(root.get("societeContactId").get("contact").get("id"), contactId);
            return criteriaBuilder.and(societePredicate, contactPredicate);
        };
    }

    /** deleted. */
    public static Specification<SocieteContact> deleted(boolean deleted) {
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
