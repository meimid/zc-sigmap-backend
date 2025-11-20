package com.sigmap.common.user_societe.request;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class UserSocieteRequest {

    private String userId;
    private Integer societeId;
    private String nom;
    private String prenom;
    private String numCin;
    private LocalDate dateDelivranceCin;
    private String tel1;
    private String tel2;
    private String email;
}
