package com.sigmap.common.paas;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class PaaId {
    private Integer paaId;
    private Integer paaVer;
}
