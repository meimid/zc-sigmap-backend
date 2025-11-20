package com.sigmap.common.ac.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcAvailablePpmResponse {
    private Integer acId;
    private String acName;
    private List<Integer> allowedYears;
}