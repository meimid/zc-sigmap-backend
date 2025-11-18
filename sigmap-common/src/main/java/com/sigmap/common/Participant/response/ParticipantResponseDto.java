package com.sigmap.common.Participant.response;

import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.entity.ParticipantFonction;
import lombok.Data;

@Data
public class ParticipantResponseDto {
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
}
