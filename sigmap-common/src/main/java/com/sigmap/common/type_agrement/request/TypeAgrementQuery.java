package com.sigmap.common.type_agrement.request;


import com.sigmap.common.type_agrement.TypeAgrement;
import com.sigmap.common.type_agrement.TypeAgrementSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeAgrementQuery  {

    private String search;
    private String libelle;
    private boolean deleted;


/**
 * Creates a {@link Specification} from the properties of the current object.
 * <p>
 * The resulting {@link Specification} will be used to query the database. The
 * specification will be created by combining the following JPA specifications:
 * <ul>
 * <li>{@link TypeAgrementSpecifications#search(String)} if the libelle property is
 * not null or blank.
 * <li>{@link TypeAgrementSpecifications#deleted(boolean)} if the deleted property is
 * true.
 * </ul>
 * <p>
 * The specifications will be combined with the {@link Specification#and(Specification)}
 * method.
 * @see TypeAgrementSpecifications
 * @return a {@link Specification} that can be used to query the database.
 */

public Specification<TypeAgrement> toSpecification() {
    Specification<TypeAgrement> spec = Specification.where(TypeAgrementSpecifications.deleted(deleted));

    if (search != null && !search.isBlank()) {
        spec = spec.and(TypeAgrementSpecifications.search(search));
    }
    if (libelle != null && !libelle.isBlank()) {
        spec = spec.and(TypeAgrementSpecifications.libelle(libelle));
    }

    return spec;
}
}
