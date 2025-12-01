package com.sigmap.common.reunion.request;

import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;

@Data
public class ReunionQuery {
    private String search;
    private Integer id;
    private Integer comId;
    private ReunionStatus status;
    private Calendar reunionDateAfter;
    private Calendar reunionDateBefore;
    private String nom;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining various specifications from the
     * {@link ReunionSpecifications} class based on which properties are set in this query object.
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)} method.
     *
     * @see ReunionSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Reunion> toSpecification() {
        // Start with the mandatory deleted status filter.
        Specification<Reunion> spec = Specification.where(ReunionSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(ReunionSpecifications.search(search));
        }

        if (id != null) {
            spec = spec.and(ReunionSpecifications.id(id));
        }

        if (comId != null) {
            spec = spec.and(ReunionSpecifications.comId(comId));
        }
        if (nom != null) {
            spec = spec.and(ReunionSpecifications.comNom(nom));
        }
        if (status != null) {
            spec = spec.and(ReunionSpecifications.status(status));
        }

        if (reunionDateAfter != null) {
            spec = spec.and(ReunionSpecifications.reunionDateAfter(reunionDateAfter));
        }

        if (reunionDateBefore != null) {
            spec = spec.and(ReunionSpecifications.reunionDateBefore(reunionDateBefore));
        }

        return spec;
    }
}
