package com.sigmap.common.societe_address.request;

import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class SocieteAddressQuery {
    private String search;
    private Integer typeId;
    private String type; // e.g. code or description

    private Integer societeId;
    private String societeName;

    private Integer paysId;
    private String pays; // description or code

    private String adresse; // full address line
    private String compAdresse;
    private String ville;
    private String codePostal;
    private String tel1;
    private String tel2;
    private String fax;
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link SocieteAddressSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link SocieteAddressSpecifications#address(String)} if the address property is not
     * null or blank.
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see SocieteAddressSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<SocieteAddress> toSpecification() {
        Specification<SocieteAddress> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.search(search));
        }

        if (typeId != null) {
            spec = spec.and(SocieteAddressSpecifications.hasTypeId(typeId));
        }
        if (type != null && !type.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasTypeString(type));
        }

        // 🔹 Filter by Societe
        if (societeId != null) {
            spec = spec.and(SocieteAddressSpecifications.hasSocieteId(societeId));
        }
        if (societeName != null && !societeName.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasSocieteName(societeName));
        }

        // 🔹 Filter by Pays
        if (paysId != null) {
            spec = spec.and(SocieteAddressSpecifications.hasPaysId(paysId));
        }
        if (pays != null && !pays.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasPaysString(pays));
        }

        // 🔹 Filter by Adresse (entity fields)
        if (adresse != null && !adresse.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasAdresse(adresse));
        }
        if (compAdresse != null && !compAdresse.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasCompAdresse(compAdresse));
        }
        if (ville != null && !ville.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasVille(ville));
        }
        if (codePostal != null && !codePostal.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasCodePostal(codePostal));
        }
        if (tel1 != null && !tel1.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasTel1(tel1));
        }
        if (tel2 != null && !tel2.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasTel2(tel2));
        }
        if (fax != null && !fax.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.hasFax(fax));
        }

        return spec;
    }
}
