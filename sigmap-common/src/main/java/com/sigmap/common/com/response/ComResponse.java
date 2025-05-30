package com.sigmap.common.com.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComResponse {
  private Integer id;
  private String nom;
  private String decretCreation;
  private String responsable;
  private Boolean special;
  private String ancrage;
  private String contact;
  private String adresse;
  private RefParamResponseDto comType;
  private List<AcResponseDto> acs;
  private List<UserDTO> users;
  private BaseEntityResponse metadata;
}
