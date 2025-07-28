package com.sigmap.common.com.response;

import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.adresse.response.AdresseResponseDto;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.keycloakAdmin.dto.ComMemberDTO;
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
  //private String responsable;
  private Boolean special;
  private String ancrage;
  private String contact;
  private String adresse;
  private RefParamResponseDto comType;
  private List<AcResponseDto> acs;
  //private List<UserDTO> users;
  private List<ComMemberDTO> users;
  private String responsableId;
  private AcResponseDto tutel;
  private String logo;
  private AdresseResponseDto theAdresse;
  private List<DocsResponseDTO> docs;
  private BaseEntityResponse metadata;
}
