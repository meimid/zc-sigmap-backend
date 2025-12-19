package com.sigmap.common.type_dossier.request;

import com.sigmap.common.type_dossier.TypeDossier;
import com.sigmap.common.type_dossier.TypeDossierSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeDossierQuery {

    private Integer id;
    private String code;
    private String libelle;
    private String description;
    private String search;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     *
     * @return {@link Specification<TypeDossier>}
     */
    public Specification<TypeDossier> toSpecification() {

        Specification<TypeDossier> spec = Specification.where(null);

        if (id != null) {
            spec = spec.and(TypeDossierSpecifications.id(id));
        }

        if (code != null && !code.isBlank()) {
            spec = spec.and(TypeDossierSpecifications.code(code));
        }

        if (libelle != null && !libelle.isBlank()) {
            spec = spec.and(TypeDossierSpecifications.libelle(libelle));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(TypeDossierSpecifications.description(description));
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and(TypeDossierSpecifications.search(search));
        }

        return spec;
    }
}