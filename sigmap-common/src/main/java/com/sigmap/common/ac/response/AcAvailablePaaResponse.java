package com.sigmap.common.ac.response;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcAvailablePaaResponse {
    private Integer acId;
    private String acName;
    private Integer allowedYear;
}
