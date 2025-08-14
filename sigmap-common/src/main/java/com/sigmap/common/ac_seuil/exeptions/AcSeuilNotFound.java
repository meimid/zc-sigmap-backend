package com.sigmap.common.ac_seuil.exeptions;

import lombok.Data;

@Data
public class AcSeuilNotFound extends RuntimeException {
    private Integer acId;
    private Integer typeMarcherId;
    private Integer modePassationId;

    public AcSeuilNotFound(String message, Integer acId, Integer typeMarcherId, Integer modePassationId) {
        super(message);
        this.acId = acId;
        this.typeMarcherId = typeMarcherId;
        this.modePassationId = modePassationId;
    }

    public AcSeuilNotFound(String message, Integer acId) {
        super(message);
        this.acId = acId;
    }

}
