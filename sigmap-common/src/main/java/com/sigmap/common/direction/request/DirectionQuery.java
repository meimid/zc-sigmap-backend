package com.sigmap.common.direction.request;

import com.sigmap.common.direction.Direction;
import com.sigmap.common.direction.DirectionSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
@Data
public class DirectionQuery {
    private String search;
    private String libelle;
    private boolean deleted;


    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link DirectionSpecifications#search(String)} if the ancrage property is
     * not null or blank.
     * <li>{@link DirectionSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     * @see DirectionSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<Direction> toSpecification() {
        Specification<Direction> spec = Specification.where(DirectionSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(DirectionSpecifications.search(search));
        }
        if (libelle != null && !libelle.isBlank()) {
            spec = spec.and(DirectionSpecifications.libelle(libelle));
        }
        return spec;
    }
}
