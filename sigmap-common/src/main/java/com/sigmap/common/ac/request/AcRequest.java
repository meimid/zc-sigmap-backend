package com.sigmap.common.ac.request;

import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.config.TypeAffCommission;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
  private MultipartFile logoFile;
  private boolean istTutel;
  private Integer comId;
  private AdresseRequest adresse;
  private TypeAffCommission typeAffCommission;

}
