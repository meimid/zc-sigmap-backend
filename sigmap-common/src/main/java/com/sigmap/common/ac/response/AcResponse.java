package com.sigmap.common.ac.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.com.response.ComResponseDto;
import com.sigmap.common.direction.response.DirectionResponseDto;
import com.sigmap.common.keycloakAdmin.dto.AcMemberDTO;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import java.util.List;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcResponse {
  private Integer id;
  private String nom;
  private String ancrage;
  private String contact;
  //private List<UserDTO> users;
  private List<AcMemberDTO> users;
  private AcResponseDto tutel;
  @JsonIgnoreProperties
  private List<DirectionResponseDto> directions;
  private String email;
  private String logo;
  private boolean isTutel;
  private ComResponseDto com;
  private AdresseResponseDto adresse;
  private BaseEntityResponse metadata;
}
