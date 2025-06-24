package com.sigmap.common.process_verbal.request;

import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.pv_docs.request.PvDocRequest;
import com.sigmap.common.pv_membre_present.PvMembrePresent;
import com.sigmap.common.pv_membre_present.request.PvMembrePresentRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalRequest {
    private Integer ppmId;
    private Integer ppmVersion;
    private List<PvMembrePresentRequest> members;
    private LocalDateTime dateDebutReunion;
    private String observation;
    private boolean decision;
    private Integer typeId;
    private ProcessStatus status;
    private List<PvDocRequest> documents;
    private Integer funcationId;
    private Integer ComId;


}
