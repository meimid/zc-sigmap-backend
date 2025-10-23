package com.sigmap.common.dyna_docs_model.exeptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DynaDocsModelAlreadyUsedExeption extends RuntimeException {
    private Integer dynaDocsModelId;
    public DynaDocsModelAlreadyUsedExeption(Integer dynaDocsModelId) {
        this.dynaDocsModelId = dynaDocsModelId;
    }
}
