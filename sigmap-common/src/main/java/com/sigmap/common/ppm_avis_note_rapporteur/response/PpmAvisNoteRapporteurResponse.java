package com.sigmap.common.ppm_avis_note_rapporteur.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

@Data
public class PpmAvisNoteRapporteurResponse {
    private String avis;
    private String user;
    private String createdAt;
    private String alfrescoId;


}
