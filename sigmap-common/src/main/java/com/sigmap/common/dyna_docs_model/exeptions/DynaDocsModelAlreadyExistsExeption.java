package com.sigmap.common.dyna_docs_model.exeptions;

import lombok.Data;

@Data
public class DynaDocsModelAlreadyExistsExeption extends RuntimeException{
    private Integer dynaDocsModelId;

    public DynaDocsModelAlreadyExistsExeption( Integer dynaDocsModelId) {
        this.dynaDocsModelId = dynaDocsModelId;
    }
}
