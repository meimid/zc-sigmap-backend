package com.sigmap.common.process_verbal_reunion.response;

import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalReunionResponse {
    private Integer id;
    private String observation;
    private Set<ProcessVerbalReunionDoc> documents = new HashSet<>();
    private BaseEntityResponse metadata;

}
