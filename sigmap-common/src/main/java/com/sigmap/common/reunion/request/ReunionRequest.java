package com.sigmap.common.reunion.request;

import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.process_verbal_reunion.request.ProcessVerbalReunionRequest;
import com.sigmap.common.process_verbal_reunion.response.ProcessVerbalReunionResponse;
import com.sigmap.common.reunionDac.request.ReunionDacRequest;
import com.sigmap.common.reunionPpm.request.ReunionPpmRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReunionRequest {
    private Integer comId;
    private Calendar reunionDate;
    private String emplacement;
    private String object;
    private ReunionStatus status;
    private String note;
    private List<ReunionPpmRequest> reunionPpms = new ArrayList<>();
    private List<ReunionDacRequest> reunionDacs = new ArrayList<>();
    private List<ParticipantRequest> participants = new ArrayList<>();
    private ProcessVerbalReunionRequest processVerbal;

}
