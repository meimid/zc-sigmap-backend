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
    private Long nbrJoursRestants;
    private String modePassation;
    private String nomAppelOffre;
    private String descriptionTypeMarche;
    private Calendar dateLimiteSoumission;
    private String alfrescoId;
}
