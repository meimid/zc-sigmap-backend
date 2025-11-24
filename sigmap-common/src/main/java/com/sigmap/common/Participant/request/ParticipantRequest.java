package com.sigmap.common.Participant.request;

import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.entity.ParticipantFonction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantRequest {
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
