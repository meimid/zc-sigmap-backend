package com.sigmap.common.process_verbal_reunion_doc.response;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalReunionDocResponse {
    private Integer pvId;
    private Integer docId;
    private DocsResponseDTO doc;
    private boolean pv;
}
