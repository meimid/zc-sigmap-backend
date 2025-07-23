
package com.sigmap.common.ac.response;

import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.com.response.ComResponseDto;
import lombok.Data;

@Data
public class AcResponseDto {
    private Integer id;
    private String nom;
    private String ancrage;
    private String contact;
    private String email;
    private String logo;
    private boolean istTutel;

}
