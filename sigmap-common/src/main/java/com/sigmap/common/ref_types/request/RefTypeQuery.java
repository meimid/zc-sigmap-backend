package com.sigmap.common.ref_types.request;


import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeSpecifications;
import org.springframework.data.jpa.domain.Specification;

public class RefTypeQuery {
    private String search;
    
    private String description;

    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link RefTypeSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link RefTypeSpecifications#description(String)} if the description
     * property is not null or blank.
     * <li>{@link RefTypeSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see RefTypeSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<RefType> toSpecification() {
        Specification<RefType> spec = Specification.where(RefTypeSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(RefTypeSpecifications.search(search));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(RefTypeSpecifications.description(description));
        }

        return spec;
    }
}
