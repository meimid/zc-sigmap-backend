package com.sigmap.common.user_ac;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.contact.Contact;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe_contact.SocieteContact;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserAcSpecifications {


    /** ids */
    public static Specification<UserAc> Ids(String userId, Integer acId) {
        return (root, query, criteriaBuilder) -> {
            Join<UserAc, Ac> joinAc = root.join("userAcId").join("ac");
            Predicate acPredicate = criteriaBuilder.equal(joinAc.get("id"), acId);
            Predicate userPredicate = criteriaBuilder.equal(root.get("userId"), userId);
            return criteriaBuilder.and(acPredicate, userPredicate);
        };
    }

    /** deleted. */
    public static Specification<UserAc> deleted(boolean deleted) {
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
    public static Specification<UserAc> activeAcsByUserId(String userId) {
        return (root, query, cb) -> {
            Join<UserAc, Ac> acJoin = root.join("ac");
            Predicate userPredicate = cb.equal(root.get("userId"), userId);
            Predicate acNotDeleted = cb.isFalse(acJoin.get("isDeleted"));
            return cb.and(userPredicate, acNotDeleted);
        };
    }
}
