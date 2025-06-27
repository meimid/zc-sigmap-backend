package com.sigmap.common.process_verbal.pv.dto;

import lombok.Data;
import org.springframework.data.repository.query.Param;

@Data
public class PvRequestDTO {
    private Integer ppmId;
    private Integer ppmVersion;
    private String processVerbalId;
}
