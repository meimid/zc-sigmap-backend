package com.sigmap.common.menu_item;

import com.sigmap.common.authority.Authority;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class MenuItemSpecifications {


    public static Specification<MenuItem> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if search is null/empty
            }
            Join<MenuItem, Authority> join = root.join("authority", JoinType.LEFT);
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("label"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("icon"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("route"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("authorityCode"), "%" + search + "%")
            );
        };
    }

    /**
     * Filter by ID
     */
    public static Specification<MenuItem> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(

                    criteriaBuilder.equal(root.get("id"), id)
            );
        };
    }

    /**
     * Filter by label.
     */
    public static Specification<MenuItem> label(String label) {
        return (root, query, criteriaBuilder) -> {
            if (label == null || label.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("label")), "%" + label.toLowerCase() + "%");
        };
    }

    /**
     * Filter by icon.
     */
    public static Specification<MenuItem> icon(String icon) {
        return (root, query, criteriaBuilder) -> {
            if (icon == null || icon.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("icon")), "%" + icon.toLowerCase() + "%");
        };
    }

    /**
     * Filter by route.
     */
    public static Specification<MenuItem> route(String route) {
        return (root, query, criteriaBuilder) -> {
            if (route == null || route.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("route")), "%" + route.toLowerCase() + "%");
        };
    }

    /**
     * Filter by orderIndex.
     */
    public static Specification<MenuItem> orderIndex(Integer orderIndex) {
        return (root, query, criteriaBuilder) -> {
            if (orderIndex == null ) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.equal(root.get("orderIndex"), orderIndex);
        };
    }

    /**
     * Filter by authorityCode.
     */
    public static Specification<MenuItem> authority(String authority) {
        return (root, query, cb) -> {

            if (authority == null || authority.isBlank()){
                return cb.conjunction();
            }
            Join<MenuItem, Authority> join = root.join("authority");
            return cb.like(cb.lower(join.get("authorityCode")), "%" + authority.toLowerCase() + "%");
        };
    }

    /**
     * Filter by authority list.
     */

    public static Specification<MenuItem> authorityIn(Collection<String> userAuthorities) {
        return (root, query, cb) -> {
            if (userAuthorities == null || userAuthorities.isEmpty()) {
                return cb.disjunction(); // returns no results
            }
            Join<MenuItem, Authority> join = root.join("authority");
            return join.get("authorityCode").in(userAuthorities);
        };
    }

    /**
     * Filter by deleted status (isDeleted field from BaseEntity).
     */
    public static Specification<MenuItem> deleted(boolean deleted) {
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
