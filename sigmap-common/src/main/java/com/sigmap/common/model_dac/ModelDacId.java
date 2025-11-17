package com.sigmap.common.model_dac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelDacId implements Serializable {
    private Integer docId;
    private Integer dacId;
    private Integer dacVer;
}
