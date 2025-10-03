package com.sigmap.common.activite.request;

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
public class ActiviteRequest {
    private String libelle;
    private List<Integer> categoriesActivitesIds=new ArrayList<>();
}
