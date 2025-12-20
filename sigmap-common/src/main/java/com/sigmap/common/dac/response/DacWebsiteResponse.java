package com.sigmap.common.dac.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DacWebsiteResponse {
    private Integer id;
    private Long nbrJoursRestants;
    private Long nbrHeuresRestants;
    private String modePassation;
    private String nomAppelOffre;
    private String nomAc;
    private String descriptionTypeMarche;
    private Calendar dateLimiteSoumission;
    private String alfrescoId;
}
