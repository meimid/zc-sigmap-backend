package com.sigmap.common.dac.request;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacSpecifications;
import com.sigmap.common.entity.ProcessStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Data
@Slf4j
public class DacQuery {
    private Integer dacId;
    private Integer dacVer;
    private Integer ppmId;
    private Integer ppmVer;
    private Integer realisationId;
    private Integer realisationVer;
    private Integer appelOffreId;
    private Integer typeMarcheId;
    private ProcessStatus status;
    private boolean deleted;

    public Specification<Dac> toSpecification() {
        Specification<Dac> spec = Specification.where(DacSpecifications.searchByDeleted(deleted));

        if (dacId != null && dacVer != null) {
            spec = spec.and(DacSpecifications.id(dacId, dacVer));
        }

        if (dacVer != null) {
            spec = spec.and(DacSpecifications.version(dacVer));
        }

        if (ppmId != null && ppmVer != null) {
            spec = spec.and(DacSpecifications.searchByPpm(ppmId, ppmVer));
        }

        if (typeMarcheId != null) {
            spec = spec.and(DacSpecifications.searchByTypeMarche(typeMarcheId));
        }

        if (status != null) {
            spec = spec.and(DacSpecifications.searchByStatus(status));
        }

        if (!deleted ) {
            spec = spec.and(DacSpecifications.searchByDeleted(deleted));
        }

        if (realisationId != null && realisationVer != null) {
            spec = spec.and(DacSpecifications.searchByRealisation(realisationId, realisationVer));
        }

        if (appelOffreId != null) {
            spec = spec.and(DacSpecifications.searchByAppelOffre(appelOffreId));
        }

        return spec;
    }


} 