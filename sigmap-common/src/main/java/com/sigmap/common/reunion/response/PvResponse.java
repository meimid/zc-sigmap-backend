package com.sigmap.common.reunion.response;

import com.sigmap.common.docs.Response.ReunionDocResponse;
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
    private List<ReunionDocResponse> pvCPMP=new ArrayList<>();
    private List<ReunionDocResponse> pvCNCMPSpecialise=new ArrayList<>();
    private List<ReunionDocResponse> pvCNCMPPermanente=new ArrayList<>();

}
