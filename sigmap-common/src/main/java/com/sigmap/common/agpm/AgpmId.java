package com.sigmap.common.agpm;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class AgpmId {
    private Integer agpmId;
    private Integer agpmVer;
}