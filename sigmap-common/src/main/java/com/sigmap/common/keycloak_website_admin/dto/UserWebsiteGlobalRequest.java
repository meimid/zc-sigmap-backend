package com.sigmap.common.keycloak_website_admin.dto;

import com.sigmap.common.societe.request.SocieteGlobalRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserWebsiteGlobalRequest {
    private UserResquestWebsiteDTO userRequest;
    private SocieteGlobalRequest societeRequest;
}