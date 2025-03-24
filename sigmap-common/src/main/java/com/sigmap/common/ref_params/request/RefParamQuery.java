package com.sigmap.common.ref_params.request;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.RefParamSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;


/**
 * A query object for {@link RefParam}. 
 * 
 * <p>This class is used to perform a search on a set of RefParams based on a 
 * combination of properties such as code, description, and deleted.
 * 
 */
@Data
public class RefParamQuery {
    private String search;

    private String code;

    private String description;

    private boolean deleted;

    
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link RefParamSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link RefParamSpecifications#code(String)} if the code property is not
     * null or blank.
     * <li>{@link RefParamSpecifications#description(String)} if the description
     * property is not null or blank.
     * <li>{@link RefParamSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see RefParamSpecifications
     * @see RefParamService
     * 
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<RefParam> toSpecification() {
        Specification<RefParam> spec = Specification.where(RefParamSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(RefParamSpecifications.search(search));
        }

        if (code != null && !code.isBlank()) {
            spec = spec.and(RefParamSpecifications.code(code));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(RefParamSpecifications.description(description));
        }

        return spec;
    }
}
