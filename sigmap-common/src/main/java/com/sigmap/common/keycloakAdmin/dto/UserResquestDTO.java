package com.sigmap.common.keycloakAdmin.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserResquestDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private List<Integer> comIds;
    private List<Integer> acIds;
    private List<String> roles;
}
