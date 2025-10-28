package com.sigmap.common.societe_dac.exceptions;


import com.sigmap.common.societe_dac.SocieteDacID;
import lombok.Data;

@Data
public class SocieteDacNotFoundException extends RuntimeException {
    Integer dacId;
    Integer dacVersion;
    Integer societeId;
    
    public SocieteDacNotFoundException(Integer dacId, Integer dacVersion, Integer societeId) {
        this.dacId = dacId;
        this.dacVersion = dacVersion;
        this.societeId = societeId;


    }
}
