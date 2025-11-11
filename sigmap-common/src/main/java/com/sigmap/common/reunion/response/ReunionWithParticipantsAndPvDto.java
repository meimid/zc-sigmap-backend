package com.sigmap.common.reunion.response;

import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.process_verbal_reunion.response.ProcessVerbalReunionResponse;
import com.sigmap.common.response.BaseEntityResponse;
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
public class ReunionWithParticipantsAndPvDto {
    private Integer id;
    private Calendar reunionDate;
    private String emplacement;
    private String object;
    private List<ParticipantResponseDto> participants;
    private ProcessVerbalReunionResponse processVerbal;
    private BaseEntityResponse metadata;



}
