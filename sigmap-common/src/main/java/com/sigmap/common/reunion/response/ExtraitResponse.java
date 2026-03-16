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
public class ExtraitResponse {
    private List<ReunionDocResponse> extraitCPMP=new ArrayList<>();
    private List<ReunionDocResponse> extraitCNCMPP=new ArrayList<>();

}
