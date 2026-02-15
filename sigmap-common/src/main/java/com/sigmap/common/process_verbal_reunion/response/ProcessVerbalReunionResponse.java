package com.sigmap.common.process_verbal_reunion.response;

import com.sigmap.common.process_verbal_reunion_doc.response.ProcessVerbalReunionDocResponse;
import com.sigmap.common.process_verbal_reunion_extrait.response.ProcessVerbalReunionExtraitRespons;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalReunionResponse {
    private Integer id;
    private String observation;
    private ProcessVerbalReunionDocResponse document;
   // private List<ProcessVerbalReunionExtraitRespons> extraits = new ArrayList<>();

}
