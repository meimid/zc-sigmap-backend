package com.sigmap.common.ac.response;
import com.sigmap.common.com.response.ComResponseDto;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcResponse {
    private Integer id;
    private String nom;
    private String ancrage;
    private String contact;
    private List<ComResponseDto> coms;
    private List<UserDTO> users;
    private BaseEntityResponse metadata;

}
