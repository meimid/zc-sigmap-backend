package com.sigmap.common.process_verbal_reunion.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import com.sigmap.common.process_verbal_reunion_doc.request.ProcessVerbalReunionDocRequest;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ProcessVerbalReunionRequest {
    private Integer id;
    private String observation;
    @JsonIgnore
    private Set<ProcessVerbalReunionDocRequest> documents = new HashSet<>();
    private List<MultipartFile> files= new ArrayList<>();

}
