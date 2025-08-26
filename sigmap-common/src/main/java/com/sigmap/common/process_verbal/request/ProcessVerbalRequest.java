package com.sigmap.common.process_verbal.request;

import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.pv_docs.request.PvDocRequest;
import com.sigmap.common.pv_membre_present.request.PvMembrePresentRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalRequest {
    private Integer ppmId;
    private Integer ppmVersion;
    private List<PvMembrePresentRequest> members;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dateDebutReunion;
    /*private String observation;
    private boolean decision;*/
    private Integer typeId;
    private ProcessStatus status;
    private List<PvDocRequest> documents;
    private Integer comId;
/*    private List<Integer> existingMemberIds = new ArrayList<>();
    private List<Integer> existingDocIds = new ArrayList<>();*/
    private String taskId;
    private String processInstanceId;
    private String processVerbalId;




}
