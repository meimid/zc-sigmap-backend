package com.sigmap.common.user_societe.response;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserSocieteResponseUpdatableDto {
    private String userId;
    private SocieteResponseDto societe;
    private Boolean responsable ;
    private String nom;
    private String prenom;
    private String numCin;
    private LocalDate dateDelivranceCin;
    private String tel1;
    private String tel2;
    private String email;
    private ActiveStatus status ;
    private BaseEntityResponse metadata;
    boolean isUpdatable = false;

}
