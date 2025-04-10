package com.sigmap.common.compte_bancaire.request;

import com.sigmap.common.com.ComSpecifications;
import com.sigmap.common.compte_bancaire.CompteBancaire;
import com.sigmap.common.compte_bancaire.CompteBancaireSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
@Data
public class CompteBancaireQuery {
    private String search;
    private String nom;
    private String agence;
    private String rib;
    private Boolean local;
    private boolean deleted;
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link ComSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link ComSpecifications#nom(String)} if the nom property is not
     * null or blank.
        * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see CompteBancaireSpecifications
     * @see CompteBancaire
     *
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<CompteBancaire> toSpecification() {
        Specification<CompteBancaire> spec = Specification.where(CompteBancaireSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(CompteBancaireSpecifications.search(search));
        }
        if (nom != null && !nom.isBlank()) {
            spec = spec.and(CompteBancaireSpecifications.nom(nom));
        }

        if (agence != null && !agence.isBlank()) {
            spec = spec.and(CompteBancaireSpecifications.agence(agence));
        }

        if (rib != null && !rib.isBlank()) {
            spec = spec.and(CompteBancaireSpecifications.rib(rib));
        }
        return spec;
    }
}
