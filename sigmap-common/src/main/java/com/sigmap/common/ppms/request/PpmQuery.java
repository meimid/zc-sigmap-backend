package com.sigmap.common.ppms.request;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ref_params.RefParam;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

@Data
public class PpmQuery {
    private String search;
    private CompositeId compositeId;
    private String ref;
    private String nom;
    private String annee;
    private BigDecimal budgetEstime;
    private RefParam status;
    private RefParam modePassation;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link PpmSpecifications#search(String)} if the search property is not null or blank.</li>
     * <li>{@link PpmSpecifications#id(CompositeId)} if the compositeId property is not null.</li>
     * <li>{@link PpmSpecifications#ref(String)} if the ref property is not null or blank.</li>
     * <li>{@link PpmSpecifications#nom(String)} if the nom property is not null or blank.</li>
     * <li>{@link PpmSpecifications#annee(String)} if the annee property is not null or blank.</li>
     * <li>{@link PpmSpecifications#budgetEstime(BigDecimal)} if the budgetEstime property is not null.</li>
     * <li>{@link PpmSpecifications#status(RefParam)} if the status property is not null.</li>
     * <li>{@link PpmSpecifications#deleted(boolean)} if the deleted property is true.</li>
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)} method.
     *
     * @see PpmSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Ppm> toSpecification() {
        Specification<Ppm> spec = Specification.where(PpmSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(PpmSpecifications.search(search));
        }

        if (compositeId != null) {
            spec = spec.and(PpmSpecifications.id(compositeId));
        }

        if (ref != null && !ref.isBlank()) {
            spec = spec.and(PpmSpecifications.ref(ref));
        }

        if (nom != null && !nom.isBlank()) {
            spec = spec.and(PpmSpecifications.nom(nom));
        }

        if (annee != null && !annee.isBlank()) {
            spec = spec.and(PpmSpecifications.annee(annee));
        }

        if (budgetEstime != null) {
            spec = spec.and(PpmSpecifications.budgetEstime(budgetEstime));
        }

        if (status != null) {
            spec = spec.and(PpmSpecifications.status(status));
        }

        return spec;
    }
}