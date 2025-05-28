package com.sigmap.common.ac.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcRequest {
  private String nom;
  private String ancrage;
  private String contact;
  //  private List<String> userIds;
}
