package com.sigmap.common.parametrage_process_dac.exeptions;

import lombok.Data;

@Data
public class ParametrageProcessDacNotFound extends RuntimeException {
    private Integer typeMarcherId;
    private Integer modePassationId;

    public ParametrageProcessDacNotFound(String message, Integer typeMarcherId, Integer modePassationId) {
        super(message);
        this.typeMarcherId = typeMarcherId;
        this.modePassationId = typeMarcherId;
    }

    public ParametrageProcessDacNotFound(String message) {
        super(message);
    }
}
