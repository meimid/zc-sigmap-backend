package com.sigmap.common.keycloakAdmin.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserResquestDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String mobile;
    private String fixe;
    private String password;
    private List<Integer> comIds;
    private List<Integer> acIds;
    private Integer acPiloteId;
    private List<String> roles;
    private boolean enable;

}
