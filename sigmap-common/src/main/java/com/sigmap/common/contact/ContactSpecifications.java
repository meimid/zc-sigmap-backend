package com.sigmap.common.contact;

import org.springframework.data.jpa.domain.Specification;

public class ContactSpecifications {
    public static Specification<Contact> search(String search) {
        return (root, query, criteriaBuilder) -> {
         ;

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("address"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("tel1"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("tel2"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("email"), "%" + search + "%")
            );
        };
    }

    /** id. */
    public  static  Specification<Contact> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** nom. */
    public static Specification<Contact> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }
    /** address. */
    public static Specification<Contact> address(String address) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("address"), "%" + address + "%");
        };
    }
    /** tel1. */
    public static Specification<Contact> tel1(String tel1) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("tel1"), "%" + tel1 + "%");
        };
    }
    /** tel2. */
    public static Specification<Contact> tel2(String tel2) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("tel2"), "%" + tel2 + "%");
        };
    }
    /** email. */
    public static Specification<Contact> email(String email) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("email"), "%" + email + "%");
        };
    }



    /** deleted. */
    public static Specification<Contact> deleted(boolean deleted) {
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
