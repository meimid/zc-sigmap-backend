package com.sigmap.common.ac.response;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcAvailablePaaResponse {
    private Integer acId;
    private String acName;
    private List<Integer> allowedYears;
}
