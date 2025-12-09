package com.sigmap.common.reunion.response;

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
public class PvResponse {
    private List<String> pvCPMP=new ArrayList<>();
    private List<String> pvCNCMPSpecialise=new ArrayList<>();
    private List<String> pvCNCMPPermanente=new ArrayList<>();

}
