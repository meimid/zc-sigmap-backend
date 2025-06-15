package com.sigmap.common.ModeDePassationSelection.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class ModeDePassationSelectionResponse {
    private Integer id;
    private String libelle;
    private BaseEntityResponse metadata;


}
