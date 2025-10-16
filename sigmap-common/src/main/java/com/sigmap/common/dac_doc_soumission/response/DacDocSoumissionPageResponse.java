package com.sigmap.common.dac_doc_soumission.response;

import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DacDocSoumissionPageResponse {
    private Integer dacId;
    private Date dateValidate;
    private Page<DocSoumissionResponse> docSoumissions;
}
