package com.sigmap.common.ModeDePassationSelection.response;

import com.sigmap.common.config.ModePassationEtape;
import com.sigmap.common.config.ModePassationType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeDePassationSelectionResponseDto {
    private Integer id;
    private String libelle;
    private boolean isRestraint;
    private ModePassationType type;
    private ModePassationEtape etapes;

}
