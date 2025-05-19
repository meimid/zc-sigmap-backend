package com.sigmap.common.keycloakAdmin.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;
    private Integer com;
    private Integer ac;
    private Long createdTimestamp;
    private boolean enabled;
}