package com.sigmap.common.ModeDePassationSelection.request;

import com.sigmap.common.config.ModePassationEtape;
import com.sigmap.common.config.ModePassationType;
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
    private ModePassationType type;
    private ModePassationEtape etapes;

}
