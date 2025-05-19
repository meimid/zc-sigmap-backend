package com.sigmap.sigmap_params.keycloakAdmin;

import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keycloak")
@RequiredArgsConstructor
public class KeycloakController {

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return keycloakAdminService.getAllUsers();
    }

    @GetMapping("/users-by-com/{com}")
    public List<UserDTO> getUsersByCom(@PathVariable Integer com) {
        return keycloakAdminService.getUsersByCom(com);
    }
    @GetMapping("/users-by-ac/{ac}")
    public List<UserDTO> getUsersByAc(@PathVariable Integer ac) {
        return keycloakAdminService.getUsersByAc(ac);
    }
    @GetMapping("/users-without-com")
    public List<UserDTO> getUsersWithoutCom() {
        return keycloakAdminService.getUsersWithoutCom();
    }

    @GetMapping("/users-without-ac")
    public List<UserDTO> getUsersWithoutAc() {
        return keycloakAdminService.getUsersWithoutAc();
    }



}
