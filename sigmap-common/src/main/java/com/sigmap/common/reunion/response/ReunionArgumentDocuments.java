package com.sigmap.common.reunion.response;

import com.sigmap.common.docs.Response.ReunionDocResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReunionArgumentDocuments {
    private Set<ReunionDocResponse> documentCPMP=new HashSet<>();
    private Set<ReunionDocResponse> documentCNCMPSpecialise=new HashSet<>();
    private Set<ReunionDocResponse> documentCNCMPPermanente=new HashSet<>();
}
