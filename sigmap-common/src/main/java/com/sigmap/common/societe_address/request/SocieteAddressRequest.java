package com.sigmap.common.societe_address.request;

import lombok.Data;

@Data
public class SocieteAddressRequest {
    private Integer societeId;
    private Integer addressId;
    private Integer typeId;

}
