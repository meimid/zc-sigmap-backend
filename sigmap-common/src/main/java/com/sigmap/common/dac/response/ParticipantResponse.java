package com.sigmap.common.dac.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantResponse {
    private String userId;
    private String nom;
    private String prenom;
    private String organisme;
    private String fonction;
    private String mail;
    private String type;
    private String fonctionReunion;
}
