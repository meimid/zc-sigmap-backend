package com.sigmap.common.com.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComRequest {
  private String nom;
  private String decretCreation;
  private String responsable;
  private Boolean special;
  private String ancrage;
  private String contact;
  private String adresse;
  private Integer comTypeId;
  // private List<Integer> acIds;
   private List<String> userIds;

}
