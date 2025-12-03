package com.sigmap.common.com.request;

import com.sigmap.common.adresse.request.AdresseRequest;
import jakarta.persistence.Column;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComRequest {
  private String nom;
 private String abreviation;
 private String nomAr;
  private String decretCreation;
 // private boolean responsable;
  private Boolean special;

  private Integer comTypeId;
  // private List<Integer> acIds;
   private List<String> userIds;
  private String responsableId;
 private Integer tutelId;
 private String logo;
 private AdresseRequest theAdresse;
 private List<MultipartFile> files;
 private List<Integer> docIds;



}
