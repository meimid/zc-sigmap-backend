package com.sigmap.common.ppms;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;


@Embeddable
@Data
public class PpmId implements Serializable {
    private Integer ppmId;
    private Integer ppmVer;
}
