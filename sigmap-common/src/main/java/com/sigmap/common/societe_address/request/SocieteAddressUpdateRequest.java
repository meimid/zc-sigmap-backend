package com.sigmap.common.societe_address.request;

import com.sigmap.common.adresse.request.AdresseRequest;
import lombok.Data;

@Data
public class SocieteAddressUpdateRequest {
    private Integer typeId;
    private AdresseRequest addressRequest;

}
