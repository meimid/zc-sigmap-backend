package com.sigmap.common.lots;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class LotsId implements java.io.Serializable {
    private Integer lotsId;
    private Integer lotsVer;
}