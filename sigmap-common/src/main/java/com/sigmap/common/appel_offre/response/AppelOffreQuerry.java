package com.sigmap.common.appel_offre.response;

import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.appel_offre.AppelOffreSpecifications;
import com.sigmap.common.entity.ModePassation;
import com.sigmap.common.entity.StatusAppelOffre;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class AppelOffreQuerry {

    private String search;
    private String reference;
    private String nom;
    private String description;
    private String etape;

    private ModePassation modePassation;
    private StatusAppelOffre statusAppelOffre;
    private Boolean lotSepares;

    private Integer ppmId;
    private Integer ppmVer;

    private Integer realisationId;
    private Integer realisationVer;

    public Specification<AppelOffre> toSpecification() {
        Specification<AppelOffre> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(AppelOffreSpecifications.search(search));
        }

        if (reference != null && !reference.isBlank()) {
            spec = spec.and(AppelOffreSpecifications.reference(reference));
        }

        if (nom != null && !nom.isBlank()) {
            spec = spec.and(AppelOffreSpecifications.nom(nom));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(AppelOffreSpecifications.description(description));
        }

        if (etape != null && !etape.isBlank()) {
            spec = spec.and(AppelOffreSpecifications.etape(etape));
        }

        if (modePassation != null) {
            spec = spec.and(AppelOffreSpecifications.modePassation(modePassation));
        }

        if (statusAppelOffre != null) {
            spec = spec.and(AppelOffreSpecifications.statusAppelOffre(statusAppelOffre));
        }

        if (lotSepares != null) {
            spec = spec.and(AppelOffreSpecifications.lotSepares(lotSepares));
        }

        if (ppmId != null) {
            spec = spec.and(AppelOffreSpecifications.ppmId(ppmId));
        }

        if (ppmVer != null) {
            spec = spec.and(AppelOffreSpecifications.ppmVer(ppmVer));
        }

        if (realisationId != null) {
            spec = spec.and(AppelOffreSpecifications.realisationId(realisationId));
        }

        if (realisationVer != null) {
            spec = spec.and(AppelOffreSpecifications.realisationVer(realisationVer));
        }

        return spec;
    }
}
