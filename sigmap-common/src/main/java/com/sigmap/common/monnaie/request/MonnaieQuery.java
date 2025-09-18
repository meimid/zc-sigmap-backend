package com.sigmap.common.monnaie.request;

import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;


/**
 * A query object for {@link Monnaie}. 
 * 
 * <p>This class is used to perform a search on a set of Monnaies based on a 
 * combination of properties such as code, description, and deleted.
 * 
 */
@Data
public class MonnaieQuery {
    private String search;

    private String code;

    private String description;

    private boolean deleted;

    private Double taux;

    
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link MonnaieSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link MonnaieSpecifications#code(String)} if the code property is not
     * null or blank.
     * <li>{@link MonnaieSpecifications#description(String)} if the description
     * property is not null or blank.
     * <li>{@link MonnaieSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see MonnaieSpecifications
     *
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Monnaie> toSpecification() {
        Specification<Monnaie> spec = Specification.where(MonnaieSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(MonnaieSpecifications.search(search));
        }

        if (code != null && !code.isBlank()) {
            spec = spec.and(MonnaieSpecifications.code(code));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(MonnaieSpecifications.description(description));
        }

        if (taux != null) {
            spec = spec.and(MonnaieSpecifications.taux(taux));
        }

        return spec;
    }
}
