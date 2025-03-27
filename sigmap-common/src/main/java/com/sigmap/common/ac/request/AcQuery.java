
package com.sigmap.common.ac.request;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcSpecifications;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.RefParamSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class AcQuery {
    private String search;
    private String nom;
    private String ancrage;
    private String contact;
    private boolean deleted;


/**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link AcSpecifications#search(String)} if the ancrage property is
     * not null or blank.
     * <li>{@link AcSpecifications#contact(String)} if the contact property is not
     * null or blank.
     * <li>{@link AcSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see AcSpecifications
     * @see AcService
     *
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<Ac> toSpecification() {
        Specification<Ac> spec = Specification.where(AcSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(AcSpecifications.search(search));
        }
        if (nom != null && !nom.isBlank()) {
            spec = spec.and(AcSpecifications.nom(nom));
        }

        if (ancrage != null && !ancrage.isBlank()) {
            spec = spec.and(AcSpecifications.ancrage(ancrage));
        }

        if (contact != null && !contact.isBlank()) {
            spec = spec.and(AcSpecifications.contact(contact));
        }

        return spec;
    }
}

