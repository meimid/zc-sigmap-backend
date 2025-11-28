
package com.sigmap.common.ac.response;

import com.sigmap.common.config.TypeAffCommission;
import lombok.Data;

@Data
public class AcResponseDto {
    private Integer id;
    private String nom;
    private String abreviation;
    private String nomAr;
    private String ancrage;
    private String contact;
    private String email;
    private String logo;
    private boolean istTutel;
    private TypeAffCommission typeAffCommission;


}
