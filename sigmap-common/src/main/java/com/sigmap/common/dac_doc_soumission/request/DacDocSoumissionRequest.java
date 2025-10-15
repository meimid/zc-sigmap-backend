package com.sigmap.common.dac_doc_soumission.request;


import lombok.Data;

import java.util.Calendar;
import java.util.Date;

@Data
public class DacDocSoumissionRequest {
    private Integer dacId;

    private Integer docSoumissionId;

    private Date dateValidate;
}
