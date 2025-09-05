package com.sigmap.common.societe_contact;

import com.sigmap.common.contact.Contact;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class SocieteContactSpecifications {


    /** ids */
    public static Specification<SocieteContact> Ids(Integer societeId, Integer contactId) {
        return (root, query, criteriaBuilder) -> {
            Join<SocieteContact, Societe> joinSociete = root.join("societeContactId").join("societe");
            Join<SocieteContact, Contact> joinContact = root.join("societeContactId").join("contact");

            Predicate societePredicate = criteriaBuilder.equal(joinSociete.get("id"), societeId);
            Predicate contactPredicate = criteriaBuilder.equal(joinContact.get("id"), contactId);
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
