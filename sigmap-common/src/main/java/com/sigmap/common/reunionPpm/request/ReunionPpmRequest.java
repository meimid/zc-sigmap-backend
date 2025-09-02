package com.sigmap.common.reunionPpm.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigmap.common.docs.Docs;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReunionPpmRequest {
    private Integer id;
    private Integer version;
    private String processInstanceId;
    private String taskId;
    private String decision;
    private String observation;
    @JsonIgnore
    private List<Docs> docsPpm = new ArrayList<>();
    private List<MultipartFile> files = new ArrayList<>();


}
