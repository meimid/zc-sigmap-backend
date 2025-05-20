package com.sigmap.sigmap_params.keycloakAdmin;

import com.sigmap.common.keycloakAdmin.dto.PasswordRequestDTO;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keycloak")
@RequiredArgsConstructor
public class KeycloakController {

    @Autowired
    private KeycloakAdminService keycloakAdminService;
    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@RequestBody UserResquestDTO userDTO) {
        keycloakAdminService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return keycloakAdminService.getAllUsers();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable String id, @RequestBody UserResquestDTO userDTO) {
        keycloakAdminService.updateUser(id, userDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> updatePassword(@PathVariable String id, @RequestBody PasswordRequestDTO passwordDTO) {
        if (passwordDTO.getNewPassword() == null || passwordDTO.getRepeatNewPassword() == null ||
                !passwordDTO.getNewPassword().equals(passwordDTO.getRepeatNewPassword())) {
            return ResponseEntity.badRequest().build();
        }

        keycloakAdminService.updatePassword(id, passwordDTO.getNewPassword());
        return ResponseEntity.ok().build();
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

    @GetMapping("/roles")
    public List<String> getAllRealmRoles() {
        return keycloakAdminService.getAllRealmRoleNames();
    }

}
