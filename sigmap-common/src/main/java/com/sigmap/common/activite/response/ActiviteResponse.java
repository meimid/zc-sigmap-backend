package com.sigmap.common.activite.response;

import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActiviteResponse {
    private Integer id;
    private String libelle;
    private List<RefParamResponseDto> categoriesActivites;
    private BaseEntityResponse metadata;
}
