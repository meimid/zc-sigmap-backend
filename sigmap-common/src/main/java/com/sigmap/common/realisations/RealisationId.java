package com.sigmap.common.realisations;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class RealisationId implements Serializable {

    private Integer realisationId;
    private Integer realisationVer;

    public RealisationId(Integer realisationId, Integer realisationVer) {
        this.realisationId = realisationId;
        this.realisationVer = realisationVer;
    }

    public RealisationId() {

    }
}
