package com.sigmap.common.Participant.request;

import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.entity.ParticipantFonction;
import lombok.Data;

@Data
public class ParticipantInvitedRequest {
    private String nom;
    private String prenom;
    private String organisme;
    private ParticipantFonction fonction;
    private String mail;
    private boolean present;
}
