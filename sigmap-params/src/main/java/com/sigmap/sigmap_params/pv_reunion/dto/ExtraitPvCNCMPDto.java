package com.sigmap.sigmap_params.pv_reunion.dto;

import com.sigmap.common.entity.ExtraitType;
import lombok.Data;

@Data
public class ExtraitPvCNCMPDto {
    private Integer reunionId;
    private Integer acId;
    private Integer ppmId;
    private Integer ppmVer;
    private Integer dacId;
    private Integer dacVer;
    private ExtraitType extraitType;
}
