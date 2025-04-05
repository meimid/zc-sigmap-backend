package com.sigmap.common.agpm.request;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.entity.CompositeId;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class AgpmQuery {
    private String search;
    private CompositeId compositeId;
    private String ref;
    private String nom;
    private String contenu;
    private Integer statusId;
    private boolean deleted;

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

        if (statusId != null) {
            spec = spec.and(AgpmSpecifications.statusId(statusId));
        }

        return spec;
    }
}