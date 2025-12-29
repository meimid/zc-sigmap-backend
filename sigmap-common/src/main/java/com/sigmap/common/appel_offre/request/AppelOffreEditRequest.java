package com.sigmap.common.appel_offre.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppelOffreEditRequest {
    private String nom;
    private String description;
    private Boolean lotSepares;
}
