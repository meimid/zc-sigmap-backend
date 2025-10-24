package com.sigmap.common.keycloak_website_admin.dto;


import com.sigmap.common.societe.Societe;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserWebsiteDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String mobile;
    private String fixe;
    private String cin;
    private LocalDate dateCIN;
    private String GenericPassword;
    private boolean emailVerified;
    private Long createdTimestamp;
    private boolean enabled;
    private Societe societe;
    private List<String> roles;

}