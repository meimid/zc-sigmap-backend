package com.sigmap.common.reunionDac.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigmap.common.docs.Docs;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReunionDacRequest {
    private Integer dacId;
    private Integer dacVer;
    private String processInstanceId;
    private String taskId;
    private String decision;
    private String observation;
    @JsonIgnore
    private List<Docs> docs = new ArrayList<>();
    private List<MultipartFile> files= new ArrayList<>();


}
