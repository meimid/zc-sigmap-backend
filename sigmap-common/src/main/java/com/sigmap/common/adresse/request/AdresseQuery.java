package com.sigmap.common.adresse.request;


import com.sigmap.common.adresse.AdressSpecifications;
import com.sigmap.common.adresse.Adresse;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class AdresseQuery {
    private String search;
    private String tel1;
    private String tel2;
    private String fax;
    private String adresse;
    private String compAdresse;
    private String codePostal;
    private String ville;
    private boolean deleted;

    public Specification<Adresse> toSpecification() {
        Specification<Adresse> spec = Specification.where(AdressSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(AdressSpecifications.search(search));
        }
        if (tel1 != null && !tel1.isBlank()) {
            spec = spec.and(AdressSpecifications.tel1(tel1));
        }

        if (tel2 != null && !tel2.isBlank()) {
            spec = spec.and(AdressSpecifications.tel2(tel2));
        }

        if (fax != null && !fax.isBlank()) {
            spec = spec.and(AdressSpecifications.fax(fax));
        }
        if (adresse != null && !adresse.isBlank()) {
            spec = spec.and(AdressSpecifications.adresse(adresse));
        }
        if (compAdresse != null && !compAdresse.isBlank()) {
            spec = spec.and(AdressSpecifications.adresse(compAdresse));
        }
        if (codePostal != null && !codePostal.isBlank()) {
            spec = spec.and(AdressSpecifications.adresse(codePostal));
        }
        if (ville != null && !ville.isBlank()) {
            spec = spec.and(AdressSpecifications.adresse(ville));
        }
        return spec;
    }

}
