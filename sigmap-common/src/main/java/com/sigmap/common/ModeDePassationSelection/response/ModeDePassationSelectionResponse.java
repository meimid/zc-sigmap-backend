package com.sigmap.common.ModeDePassationSelection.response;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeDePassationSelectionResponse {
    private Integer id;
    private String libelle;
    private List<RefParamResponseDto> typeMarchers;
    private BaseEntityResponse metadata;


}
