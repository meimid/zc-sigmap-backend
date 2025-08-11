package com.sigmap.common.delais_reglementaires.exceptions;

import lombok.Data;

@Data
public class DelaisReglementairesNotFound extends RuntimeException {
    private Integer typeMarcherId;
    private Integer modePassationId;

    public DelaisReglementairesNotFound(String message, Integer typeMarcherId, Integer modePassationId) {
        super(message);
        this.typeMarcherId = typeMarcherId;
        this.modePassationId = modePassationId;
    }

    public DelaisReglementairesNotFound(String message) {
        super(message);
    }

    public DelaisReglementairesNotFound(String message, Integer typeMarcherId) {
        super(message);
        this.typeMarcherId = typeMarcherId;
    }
}