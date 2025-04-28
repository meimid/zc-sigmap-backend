package com.sigmap.common.societe_address.request;

import lombok.Data;

@Data
public class SocieteAddressRequest {
    private Integer typeId;
    private String address;
    private Integer societeId;
}
