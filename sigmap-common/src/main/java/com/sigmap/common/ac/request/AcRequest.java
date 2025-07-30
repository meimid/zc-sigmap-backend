package com.sigmap.common.ac.request;

import com.sigmap.common.adresse.request.AdresseRequest;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcRequest {
  private String nom;
  private List<String> userIds;
  private Integer tutelId;
  private String email;
  private String logo;
  private boolean istTutel;
  private Integer comId;
  private AdresseRequest adresse;
}
