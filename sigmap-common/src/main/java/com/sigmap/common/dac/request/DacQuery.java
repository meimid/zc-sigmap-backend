package com.sigmap.common.dac.request;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacSpecifications;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Data
@Slf4j
public class DacQuery {
    private String search;
    private Integer dacId;
    private Integer dacVer;
    private Integer ppmId;
    private Integer ppmVer;
    private Integer categoryId;
    private String status;
    private boolean deleted;

    public Specification<Dac> toSpecification() {
        Specification<Dac> spec = Specification.where(DacSpecifications.searchByDeleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(DacSpecifications.searchByDeleted(deleted));
        }

        return spec;
    }


} 