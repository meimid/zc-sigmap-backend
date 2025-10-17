package com.sigmap.common.user_societe.response;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.Data;

import java.util.Date;

@Data
public class UserSocieteResponseDto {
    private String userId;
    private SocieteResponseDto societe;
    private Boolean responsable ;
    private String nom;
    private String prenom;
    private String numCin;
    private Date dateDelivranceCin;
    private String tel1;
    private String tel2;
    private String email;
    private ActiveStatus status ;
}
