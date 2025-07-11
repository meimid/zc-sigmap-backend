package com.sigmap.common.pv_membre_present.request;

import com.sigmap.common.entity.Foncation;
import lombok.Data;

@Data
public class PvMembrePresentRequest {
    private Integer ppmId;
    private Integer ppmVersion;
    private String processVerbalId;
    private Integer pvMembrePresentId;
    private String nom;
    private String prenom;
    private boolean present;
    private Integer fonctionId;
    private String userId;
    private Foncation foncation;

}
