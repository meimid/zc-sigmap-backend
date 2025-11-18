package com.sigmap.common.Participant.response;

import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.entity.ParticipantFonction;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data

public class ParticipantResponse {
    private Integer id;
    private Integer reunionId;
    private ParticipantType type;
    private String nom;
    private String prenom;
    private String organisme;
    private String fonction;
    private String mail;
    private String userId;
    private boolean present;
    private ParticipantFonction fonctionReunion;
    private BaseEntityResponse metadata;

}
