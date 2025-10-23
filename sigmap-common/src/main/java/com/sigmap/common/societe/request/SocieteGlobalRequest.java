package com.sigmap.common.societe.request;

import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteGlobalRequest {
    SocieteRequest societeRequest;
    AdresseRequest addressRequest;
    UserSocieteRequest userSocieteRequest;
}
