package com.sigmap.common.paas.request;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import com.sun.istack.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaaRequest {

    private Integer id;

    private Integer version;

    private String ref;

    private String nom;

    private String annee;

    private Integer acId;

    private BigDecimal budgetEstime;

    private RefParam status;

    private RefParam modePassation;

    private List<CompositeId> realisationIds;
    // toDO: nejah
    private List<MultipartFile> files;

}