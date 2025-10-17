package com.sigmap.common.ModeDePassationSelection.request;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeDePassationSelectionRequest {
    private String libelle;
    private List <Integer> typeMarchersIds=new ArrayList<>();
    private boolean isRestraint;

}
