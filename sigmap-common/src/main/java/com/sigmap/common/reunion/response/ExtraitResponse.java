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
public class ExtraitResponse {
    private List<String> extraitCPMP=new ArrayList<>();
    private List<String> extraitCNCMPP=new ArrayList<>();

}
