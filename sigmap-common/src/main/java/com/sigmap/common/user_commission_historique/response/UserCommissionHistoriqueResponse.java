package com.sigmap.common.user_commission_historique.response;

import com.sigmap.common.entity.ActiveStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCommissionHistoriqueResponse {
    private Integer id;
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String mobile;
    private String fixe;
    private Integer comId;
    private ActiveStatus status;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}
