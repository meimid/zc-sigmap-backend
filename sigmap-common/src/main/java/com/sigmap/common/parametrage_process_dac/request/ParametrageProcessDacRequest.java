package com.sigmap.common.parametrage_process_dac.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParametrageProcessDacRequest {
    private Integer typeMarcherId;
    private Integer modePassationId;
    private String process;
}
