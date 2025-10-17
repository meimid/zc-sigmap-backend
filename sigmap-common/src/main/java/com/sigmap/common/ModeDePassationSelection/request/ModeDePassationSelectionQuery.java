package com.sigmap.common.ModeDePassationSelection.request;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeDePassationSelectionQuery {
    private String search;
    private String libelle;
    private boolean deleted;
    private boolean isRestraint;


    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link ModeDePassationSelectionSpecifications#search(String)} if the libelle property is
     * not null or blank.
     * <li>{@link ModeDePassationSelectionSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     * @see ModeDePassationSelectionSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<ModeDePassationSelection> toSpecification() {
        Specification<ModeDePassationSelection> spec = Specification.where(ModeDePassationSelectionSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(ModeDePassationSelectionSpecifications.search(search));
        }
        if (libelle != null && !libelle.isBlank()) {
            spec = spec.and(ModeDePassationSelectionSpecifications.libelle(libelle));
        }
        if (isRestraint) {
            spec = spec.and(ModeDePassationSelectionSpecifications.isRestraint(isRestraint));
        }

        return spec;
    }
}
