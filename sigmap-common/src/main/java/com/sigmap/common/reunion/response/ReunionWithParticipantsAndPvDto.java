package com.sigmap.common.reunion.response;

import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.process_verbal_reunion.response.ProcessVerbalReunionResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReunionWithParticipantsAndPvDto {
    private List<ParticipantResponseDto> participants;
    private ProcessVerbalReunionResponse processVerbal;


}
