package com.sigmap.common.reunion.request;

import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.reunionDac.request.ReunionDacRequest;
import com.sigmap.common.reunionPpm.request.ReunionPpmRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private List<ReunionPpmRequest> reunionPpms;
    private List<ReunionDacRequest> reunionDacs;
    private List<ParticipantRequest> participants;
}
