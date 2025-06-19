package com.sigmap.common.com.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.Data;

@Data
public class ComResponseDto {
  private Integer id;
  private String nom;
  private String decretCreation;
  //private String responsable;
  private Boolean special;
  private String ancrage;
  private String contact;
  private String adresse;
  private RefParamResponseDto comType;
  private String responsableId;

}
