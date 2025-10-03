package com.sigmap.common.activite.request;


import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.ActiviteSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActiviteQuery {
    private String search;
    private String libelle;
    private boolean deleted;


    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link ActiviteSpecifications#search(String)} if the libelle property is
     * not null or blank.
     * <li>{@link ActiviteSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     * @see ActiviteSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<Activite> toSpecification() {
        Specification<Activite> spec = Specification.where(ActiviteSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(ActiviteSpecifications.search(search));
        }
        if (libelle != null && !libelle.isBlank()) {
            spec = spec.and(ActiviteSpecifications.libelle(libelle));
        }

        return spec;
    }
}
