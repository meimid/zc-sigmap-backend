package com.sigmap.common.dyna_docs_model.exeptions;

import lombok.Data;

@Data
public class DynaDocsModelNotFoundExeption extends RuntimeException {
    private Integer dynaDocsModelId;

    public DynaDocsModelNotFoundExeption(Integer dynaDocsModelId) {
        this.dynaDocsModelId = dynaDocsModelId;
    }
}
