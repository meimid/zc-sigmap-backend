package com.sigmap.common.contact.request;

import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.ContactSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
@Data
public class ContactQuery {
    private String search;
    private String nom;
    private String address;
    private String tel1;
    private String tel2;
    private String email;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link ContactSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link ContactSpecifications#nom(String)} if the nom property is not
     * null or blank.
     * <li>{@link ContactSpecifications#address(String)} if the address
     * property is not null or blank.
     * <li>{@link ContactSpecifications#tel1(String)} if the tel1
     * property is not null or blank.
     * <li>{@link ContactSpecifications#tel2(String)} if the tel2
     * property is not null or blank.
     *   * <li>{@link ContactSpecifications#email(String)} if the email
     *      * property is not null or blank.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see ContactSpecifications
     * @see Contact
     *
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<Contact> toSpecification() {
        Specification<Contact> spec = Specification.where(ContactSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(ContactSpecifications.search(search));
        }
        if (nom != null && !nom.isBlank()) {
            spec = spec.and(ContactSpecifications.nom(nom));
        }

        if (address != null && !address.isBlank()) {
            spec = spec.and(ContactSpecifications.address(address));
        }

        if (tel1 != null && !tel1.isBlank()) {
            spec = spec.and(ContactSpecifications.tel1(tel1));
        }
        if (tel2 != null && !tel2.isBlank()) {
            spec = spec.and(ContactSpecifications.tel2(tel2));
        }
        if (email != null && !email.isBlank()) {
            spec = spec.and(ContactSpecifications.email(email));
        }


        return spec;
    }
}
