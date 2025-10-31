package com.sigmap.common.devise_history.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviseHistoryResponse {

    private Integer id;
    private Integer deviseId;
    private String code;
    private String description;
    private Double taux;
    private Calendar dateDebut;
    private Calendar dateFin;
    private String action;
    //private String UserId;
    private String changedBy;
}
