package com.sigmap.common.user_societe.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateNewNonResponsableRequest {
    private Integer societeId;
    private String nom;
    private String prenom;
    private String numCin;
    private LocalDate dateDelivranceCin;
    private String tel1;
    private String tel2;
    private String email;
}
