package com.sigmap.common.type_agrement.response;

import com.sigmap.common.ref_types.response.RefTypeResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class TypeAgrementResponse {

    private Integer id;
    private String libelle;
    private RefTypeResponseDto organismeDeDeliveranceAgrements;
    private BaseEntityResponse metadata;
}
