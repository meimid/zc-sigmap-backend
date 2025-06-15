package com.sigmap.common.dac.response;

import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class DacResponce {
    private Integer dacId;
    private Integer dacVer;
    private Ppm ppm;
    private RefParam category;
    private Integer statusId;
    private BaseEntityResponse metadata;
}
