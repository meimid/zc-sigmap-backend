package com.sigmap.common.agpm;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class AgpmId {

    private Integer id;
    private Integer agpm_ver;
}
