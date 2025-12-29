package com.sigmap.common.ModeDePassationSelection.response;

import com.sigmap.common.config.ModePassationEtape;
import com.sigmap.common.config.ModePassationType;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeDePassationSelectionResponseDto {
    private Integer id;
    private String libelle;
    private List<RefParamResponseDto> typeMarchers;
    private boolean isRestraint;
    private ModePassationType type;
    private ModePassationEtape etapes;

}
