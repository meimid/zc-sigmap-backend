package com.sigmap.common.reunion.response;

import com.sigmap.common.com.response.ComSelectResponseDto;
import com.sigmap.common.process_verbal_reunion.response.ProcessVerbalReunionResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PvAndComResponse {
    private List<String> CPMP=new ArrayList<>();
    private List<String> CNCMPSpecialise=new ArrayList<>();
    private List<String> CNCMPPermanente=new ArrayList<>();

}
