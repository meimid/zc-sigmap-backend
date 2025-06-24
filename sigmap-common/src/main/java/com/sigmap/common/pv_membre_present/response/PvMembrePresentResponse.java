package com.sigmap.common.pv_membre_present.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sigmap.common.entity.Foncation;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PvMembrePresentResponse {

    private String processVerbalId;
    private Integer pvMembrePresentId;
    @JsonIgnoreProperties
    private ProcessVerbal processVerbal;
    private String nom;
    private String prenom;
    private boolean present;
    private RefParam fonctionId;
    private String userId;
    private Foncation foncation;

    private BaseEntityResponse metadata;

}
