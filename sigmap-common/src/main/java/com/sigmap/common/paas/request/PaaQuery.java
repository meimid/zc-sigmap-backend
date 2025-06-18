package com.sigmap.common.paas.request;

import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;

import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

@Data
public class PaaQuery {
    private String search;
    private CompositeId compositeId;
    private String ref;
    private String nom;
    private String annee;
    private BigDecimal budgetEstime;
    private ProcessStatus status;
    private RefParam modePassation;
    private String remarque;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link PaaSpecifications#search(String)} if the search property is not null or blank.</li>
     * <li>{@link PaaSpecifications#id(CompositeId)} if the compositeId property is not null.</li>
     * <li>{@link PaaSpecifications#ref(String)} if the ref property is not null or blank.</li>
     * <li>{@link PaaSpecifications#nom(String)} if the nom property is not null or blank.</li>
     * <li>{@link PaaSpecifications#annee(String)} if the annee property is not null or blank.</li>
     * <li>{@link PaaSpecifications#budgetEstime(BigDecimal)} if the budgetEstime property is not null.</li>
     * <li>{@link PaaSpecifications#status(ProcessStatus)} if the status property is not null.</li>
     * <li>{@link PaaSpecifications#deleted(boolean)} if the deleted property is true.</li>
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)} method.
     *
     * @see PaaSpecifications
     * @see PaaService
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Paa> toSpecification() {
        Specification<Paa> spec = Specification.where(PaaSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(PaaSpecifications.search(search));
        }

        if (compositeId != null) {
            spec = spec.and(PaaSpecifications.id(compositeId));
        }

        if (ref != null && !ref.isBlank()) {
            spec = spec.and(PaaSpecifications.ref(ref));
        }

        if (nom != null && !nom.isBlank()) {
            spec = spec.and(PaaSpecifications.nom(nom));
        }

        if (annee != null && !annee.isBlank()) {
            spec = spec.and(PaaSpecifications.annee(annee));
        }

        if (budgetEstime != null) {
            spec = spec.and(PaaSpecifications.budgetEstime(budgetEstime));
        }

        if (status != null) {
            spec = spec.and(PaaSpecifications.status(status));
        }

        return spec;
    }
}