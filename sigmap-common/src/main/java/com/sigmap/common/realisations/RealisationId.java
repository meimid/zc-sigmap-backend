package com.sigmap.common.realisations;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class RealisationId implements Serializable {
    private Integer realisationId;
    private Integer realisationVer;
}
