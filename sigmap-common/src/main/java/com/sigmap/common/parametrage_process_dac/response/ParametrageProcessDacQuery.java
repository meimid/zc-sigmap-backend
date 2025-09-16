package com.sigmap.common.parametrage_process_dac.response;


import com.sigmap.common.parametrage_process_dac.ParametrageProcessDac;
import com.sigmap.common.parametrage_process_dac.ParametrageProcessDacSpecification;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class ParametrageProcessDacQuery {
    private Integer typeMarcherId;
    private Integer modePassationId;
    private String processExact;
    private String processLike;

    public Specification<ParametrageProcessDac> toSpecification() {
        Specification<ParametrageProcessDac> spec = Specification.where(null);

        if (typeMarcherId != null) {
            spec = spec.and(ParametrageProcessDacSpecification.typeMarcherId(typeMarcherId));
        }
        if (modePassationId != null) {
            spec = spec.and(ParametrageProcessDacSpecification.modePassationId(modePassationId));
        }
        if (processExact != null) {
            spec = spec.and(ParametrageProcessDacSpecification.processExact(processExact));
        }
        if (processLike != null) {
            spec = spec.and(ParametrageProcessDacSpecification.processLike(processLike));
        }
        return spec;
    }
}
