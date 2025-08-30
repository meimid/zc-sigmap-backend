package com.sigmap.common.process_verbal_reunion.response;

import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import com.sigmap.common.process_verbal_reunion_doc.response.ProcessVerbalReunionDocResponse;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalReunionResponse {
    private Integer id;
    private String observation;
    private List<ProcessVerbalReunionDocResponse> documents = new ArrayList<>();
    private BaseEntityResponse metadata;

}
