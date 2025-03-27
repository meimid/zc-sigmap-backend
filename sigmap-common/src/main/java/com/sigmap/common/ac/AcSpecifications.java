
package com.sigmap.common.ac;
import org.springframework.data.jpa.domain.Specification;

public class AcSpecifications {
    public static Specification<Ac> search(String search) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("ancrage"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("contact"), "%" + search + "%"));
        };
    }


/** id. */

    public  static  Specification<Ac> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }


/** ancrage. */

    public static Specification<Ac> ancrage(String ancrage) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("ancrage"), "%" + ancrage + "%");
        };
    }

    /** nom. */
    public static Specification<Ac> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

/** code. */

    public static Specification<Ac> contact(String contact) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("contact"), "%" + contact + "%");
        };
    }


/** deleted. */

    public static Specification<Ac> deleted(boolean deleted) {
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

