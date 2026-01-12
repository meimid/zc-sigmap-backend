package com.sigmap.common.dyna_docs_model.exeptions;

import lombok.Data;
@Data
public class DynaDocsModelRequiredExeption extends RuntimeException {

    public DynaDocsModelRequiredExeption(String message) {
        super(message);
    }}
