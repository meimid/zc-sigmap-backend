package com.sigmap.common.dac_doc_soumission.response;

import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;

@Data
public class DacDocSoumissionResponse {
    private Integer dacId;
    private DocSoumissionResponse docSoumission;
    private Date dateValidate;

}
