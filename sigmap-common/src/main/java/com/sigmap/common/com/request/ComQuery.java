package com.sigmap.common.com.request;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class ComQuery {
    private String search;
    private String nom;
    private String decretCreation;
    private String responsable;
    private Boolean special;
    private String ancrage;
    private String contact;
    private String adresse;
    private String membres;
    private boolean deleted;
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link ComSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link ComSpecifications#decretCreation(String)} if the decretCreation property is not
     * null or blank.
     * <li>{@link ComSpecifications#responsable(String)} if the responsable
     * property is not null or blank.
     * property is not null or blank.
     * <li>{@link ComSpecifications#ancrage(String)} if the ancrage
     * property is not null or blank.
     * <li>{@link ComSpecifications#contact(String)} if the contact
     * property is not null or blank.
     * <li>{@link ComSpecifications#adresse(String)} if the adresse
     * * property is not null or blank.
     * <li>{@link ComSpecifications#membres(String)} if the membres
     * property is not null or blank.
     * <li>{@link ComSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see ComSpecifications*
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<Com> toSpecification() {
        Specification<Com> spec = Specification.where(ComSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(ComSpecifications.search(search));
        }
        if (nom != null && !nom.isBlank()) {
            spec = spec.and(ComSpecifications.nom(nom));
        }

        if (decretCreation != null && !decretCreation.isBlank()) {
            spec = spec.and(ComSpecifications.decretCreation(decretCreation));
        }

        if (responsable != null && !responsable.isBlank()) {
            spec = spec.and(ComSpecifications.responsable(responsable));
        }

        if (ancrage != null && !ancrage.isBlank()) {
            spec = spec.and(ComSpecifications.ancrage(ancrage));
        }
        if (contact != null && !contact.isBlank()) {
            spec = spec.and(ComSpecifications.contact(contact));
        }
        if (adresse != null && !adresse.isBlank()) {
            spec = spec.and(ComSpecifications.adresse(adresse));
        }
        if (membres != null && !membres.isBlank()) {
            spec = spec.and(ComSpecifications.responsable(membres));
        }

        return spec;
    }
}
