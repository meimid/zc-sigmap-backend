package com.sigmap.common.dac;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DacId implements Serializable {

    @Column(name = "dac_id")
    private Integer dacId;
    @Column(name = "dac_ver")
    private Integer dacVer;
}
