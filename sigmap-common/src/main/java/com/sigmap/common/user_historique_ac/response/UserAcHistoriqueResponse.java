package com.sigmap.common.user_historique_ac.response;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAcHistoriqueResponse {
    private Integer id;
    private String userId;
    private String userName;
    private Ac ac;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private ActiveStatus status;
}
