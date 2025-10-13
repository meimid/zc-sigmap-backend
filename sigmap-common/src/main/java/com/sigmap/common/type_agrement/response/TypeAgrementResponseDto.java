package com.sigmap.common.type_agrement.response;

import com.sigmap.common.ref_types.response.RefTypeResponseDto;
import lombok.Data;

@Data
public class TypeAgrementResponseDto {
    private Integer id;
    private String libelle;
    private RefTypeResponseDto organismeDeDeliveranceAgrements;
}
