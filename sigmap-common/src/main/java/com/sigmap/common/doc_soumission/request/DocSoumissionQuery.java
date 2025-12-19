package com.sigmap.common.doc_soumission.request;


import com.sigmap.common.doc_soumission.DocSoumission;
import com.sigmap.common.doc_soumission.DocSoumissionSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocSoumissionQuery {
    private String search;
    private String nom;
    private String typeDossier;
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link DocSoumissionSpecifications#search(String)} if the libelle property is
     * not null or blank.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     * @see DocSoumissionSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<DocSoumission> toSpecification() {
        Specification<DocSoumission> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(DocSoumissionSpecifications.search(search));
        }
        if (nom != null && !nom.isBlank()) {
            spec = spec.and(DocSoumissionSpecifications.nom(nom));
        }
        if (typeDossier != null) {
            spec = spec.and(DocSoumissionSpecifications.typeDossier(typeDossier));
        }

        return spec;
    }

}
