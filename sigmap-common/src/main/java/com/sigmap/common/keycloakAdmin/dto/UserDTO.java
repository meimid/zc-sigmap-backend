package com.sigmap.common.keycloakAdmin.dto;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.com.Com;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private boolean emailVerified;
    private List<Com> coms;
    private List<Ac> acs;
    private Long createdTimestamp;
    private boolean enabled;
    private List<String> roles;

}