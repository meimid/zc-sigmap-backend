package com.sigmap.common.reunion.response;

import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.com.response.ComResponseDto;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.process_verbal_reunion.response.ProcessVerbalReunionResponse;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.reunionDac.response.ReunionDacResponse;
import com.sigmap.common.reunionPpm.response.ReunionPpmResponse;
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
public class ReunionResponse {
    private Integer id;
    private ComResponseDto com;
    private Calendar reunionDate;
    private String emplacement;
    private String object;
    private ReunionStatus status;
    private String note;
    private List<ReunionPpmResponse> reunionPpms;
    private List<ReunionDacResponse> reunionDacs;
    private List<ParticipantResponseDto> participants;
     private ProcessVerbalReunionResponse processVerbal;
    private BaseEntityResponse metadata;

}
