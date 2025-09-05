package com.sigmap.common.keycloakAdmin.dto;


import lombok.Data;

@Data
public class RoleDTO {
    private String name;   // nom technique (ex: VALIDATEUR_AC)
    private String label;  // description lisible (ex: Validateur AC)

    public RoleDTO(String name, String label) {
        this.name = name;
        this.label = label;
    }
}
