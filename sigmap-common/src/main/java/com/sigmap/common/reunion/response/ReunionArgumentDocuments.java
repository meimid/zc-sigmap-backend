package com.sigmap.common.reunion.response;

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
    private Set<String> documentCPMP=new HashSet<>();
    private Set<String> documentCNCMPSpecialise=new HashSet<>();
    private Set<String> documentCNCMPPermanente=new HashSet<>();
}
