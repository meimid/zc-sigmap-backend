package com.sigmap.common.societe.request;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SocieteRequest {
    private String nom;
    private Integer typeId;
    private Integer paysId;
    private String nni;
    private Integer categorieSoumissionnaireId;
    private List<Integer> activiteIds=new ArrayList<>();
    private String nif;
    private Date dateNif;
    private Integer numCnss;
    private ActiveStatus status;


}
