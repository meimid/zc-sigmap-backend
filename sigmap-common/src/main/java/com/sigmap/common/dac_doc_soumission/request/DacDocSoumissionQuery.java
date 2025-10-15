package com.sigmap.common.dac_doc_soumission.request;

import com.sigmap.common.dac_doc_soumission.DacDocSoumission;
import com.sigmap.common.dac_doc_soumission.DacDocSoumissionSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;

@Data
public class DacDocSoumissionQuery {

    private Integer dacId;
    private Integer dacVer;
    private Integer docSoumissionId;
    private Calendar dateValidateAfter;
    private Calendar dateValidateBefore;
    private Calendar dateValidateExact;


    /**
     * Creates a {@link Specification} from the properties of the current object.
     *
     * This method dynamically combines filters from {@link DacDocSoumissionSpecifications}
     * based on which fields are set.
     *
     * @return a {@link Specification} to query {@link DacDocSoumission}
     */
    public Specification<DacDocSoumission> toSpecification() {
        Specification<DacDocSoumission> spec = Specification.where(null);

        if (dacId != null) {
            spec = spec.and(DacDocSoumissionSpecifications.byDacId(dacId));
        }

        if (dacVer != null) {
            spec = spec.and(DacDocSoumissionSpecifications.byDacVer(dacVer));
        }

        if (docSoumissionId != null) {
            spec = spec.and(DacDocSoumissionSpecifications.byDocSoumissionId(docSoumissionId));
        }

        if (dateValidateExact != null) {
            spec = spec.and(DacDocSoumissionSpecifications.dateValidateEquals(dateValidateExact));
        } else if (dateValidateAfter != null && dateValidateBefore != null) {
            spec = spec.and(DacDocSoumissionSpecifications.byDateValidateBetween(dateValidateAfter, dateValidateBefore));
        } else if (dateValidateAfter != null) {
            spec = spec.and(DacDocSoumissionSpecifications.dateValidateAfter(dateValidateAfter));
        } else if (dateValidateBefore != null) {
            spec = spec.and(DacDocSoumissionSpecifications.dateValidateBefore(dateValidateBefore));
        }

        return spec;
    }
}
