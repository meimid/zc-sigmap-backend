package com.sigmap.common.dac_doc_soumission.request;


import lombok.Data;

import java.util.Date;

@Data
public class DacDocSoumissionCreateRequest {
    private Integer dacId;

    private Integer docSoumissionId;

    private Date dateValidate;
}
