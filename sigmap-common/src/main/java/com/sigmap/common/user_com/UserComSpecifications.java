package com.sigmap.common.user_com;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.com.Com;
import com.sigmap.common.user_ac.UserAc;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserComSpecifications {


    /** ids */
    public static Specification<UserCom> Ids(String userId, Integer comId) {
        return (root, query, criteriaBuilder) -> {
            Join<UserCom, Com> joinCom = root.join("userComId").join("com");
            Predicate comPredicate = criteriaBuilder.equal(joinCom.get("id"), comId);
            Predicate userPredicate = criteriaBuilder.equal(root.get("userId"), userId);
            return criteriaBuilder.and(comPredicate, userPredicate);
        };
    }

    /** deleted. */
    public static Specification<UserCom> deleted(boolean deleted) {
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
