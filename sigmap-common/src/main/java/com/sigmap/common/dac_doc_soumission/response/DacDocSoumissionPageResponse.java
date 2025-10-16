package com.sigmap.common.dac_doc_soumission.response;

import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DacDocSoumissionPageResponse {
    private Integer dacId;
    private List<DacDocSoumissionWithOutDacId> content;
    private Pageable pageable;
    private boolean last;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private Sort sort;
    private boolean first;
    private int numberOfElements;
    private boolean empty;
}
