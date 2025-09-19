package com.sigmap.common.agpm.request;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class AgpmQuery {
    private String search;
    private CompositeId compositeId;
    private String ref;
    private String nom;
    private String contenu;
    private ProcessStatus status;
    private boolean deleted;
    private Integer acId;
    private Integer year;
    private Boolean actualise;
    private Integer nbrActualisation;

    public Specification<Agpm> toSpecification() {
        Specification<Agpm> spec = Specification.where(AgpmSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(AgpmSpecifications.search(search));
        }

        if (compositeId != null) {
            spec = spec.and(AgpmSpecifications.id(compositeId));
        }

        if (ref != null && !ref.isBlank()) {
            spec = spec.and(AgpmSpecifications.ref(ref));
        }

        if (nom != null && !nom.isBlank()) {
            spec = spec.and(AgpmSpecifications.nom(nom));
        }

        if (contenu != null && !contenu.isBlank()) {
            spec = spec.and(AgpmSpecifications.contenu(contenu));
        }

        if (status != null) {
            spec = spec.and(AgpmSpecifications.status(status));
        }

        if (acId != null) {
            spec = spec.and(AgpmSpecifications.withAcId(acId));
        }

        if (year != null) {
            spec = spec.and(AgpmSpecifications.withYear(year));
        }

        if (actualise != null) {
            spec = spec.and(AgpmSpecifications.actualise(actualise));
        }

        if (nbrActualisation != null) {
            spec = spec.and(AgpmSpecifications.nbrActualisation(nbrActualisation));
        }

        return spec;
    }
}
