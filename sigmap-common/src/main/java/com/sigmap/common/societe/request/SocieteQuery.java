package com.sigmap.common.societe.request;

import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class SocieteQuery {
    private String search;
    private String nom;
    private String nni;
    private String categorieSoumissionnaireId;
    private boolean deleted;
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by Societebining the following JPA specifications:
     * <ul>
     * <li>{@link SocieteSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link SocieteSpecifications#nom(String)} if the nom property is not
     * null or blank.
     * <li>{@link SocieteSpecifications#nni(String)} if the nni
     * property is not null or blank.
     * <li>{@link SocieteSpecifications#categorieSoumissionnaireId(String)} if the categorieSoumissionnaireId
     * property is not null or blank.
     * <li>{@link SocieteSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be Societebined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see SocieteSpecifications
     * @see SocieteService
     *
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<Societe> toSpecification() {
        Specification<Societe> spec = Specification.where(SocieteSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(SocieteSpecifications.search(search));
        }

        if (nom != null && !nom.isBlank()) {
            spec = spec.and(SocieteSpecifications.nom(nom));
        }
        if (nni != null && !nni.isBlank()) {
            spec = spec.and(SocieteSpecifications.nni(nni));
        }
        if (categorieSoumissionnaireId != null && !categorieSoumissionnaireId.isBlank()) {
            spec = spec.and(SocieteSpecifications.nom(categorieSoumissionnaireId));
        }
        return spec;
    }
}
