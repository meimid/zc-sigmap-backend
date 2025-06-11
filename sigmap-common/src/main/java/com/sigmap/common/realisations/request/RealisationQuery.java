package com.sigmap.common.realisations.request;

import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import com.sigmap.common.realisations.RealisationService;
/**
 * Query object for filtering Realisation entities.
 */
@Data
public class RealisationQuery {
    private String search; // General search across realisationEnvisagees
    private String realisationEnvisagees; // Specific filter for realisationEnvisagees
    private Integer serviceId; // Filter by serviceId
    private Integer directionId; // Filter by directionId
    private boolean deleted; // Filter by isDeleted from BaseEntity

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link RealisationSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link RealisationSpecifications#realisationEnvisagees(String)} if the realisationEnvisagees
     * property is not null or blank.
     * <li>{@link RealisationSpecifications#serviceId(Integer)} if the serviceId property is
     * not null.
     * <li>{@link RealisationSpecifications#directionId(Integer)} if the directionId property is
     * not null.
     * <li>{@link RealisationSpecifications#deleted(boolean)} always applied based on the deleted property.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see RealisationSpecifications
     * @see RealisationService
     *
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Realisation> toSpecification() {
        Specification<Realisation> spec = Specification.where(RealisationSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(RealisationSpecifications.search(search));
        }

        if (realisationEnvisagees != null && !realisationEnvisagees.isBlank()) {
            spec = spec.and(RealisationSpecifications.realisationEnvisagees(realisationEnvisagees));
        }

        if (serviceId != null) {
            spec = spec.and(RealisationSpecifications.serviceId(serviceId));
        }

        if (directionId != null) {
            spec = spec.and(RealisationSpecifications.directionId(directionId));
        }

        return spec;
    }
}