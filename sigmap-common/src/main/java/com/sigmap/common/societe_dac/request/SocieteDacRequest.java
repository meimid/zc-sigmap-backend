package com.sigmap.common.societe_dac.request;

import com.sigmap.common.entity.MethodePaiement;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SocieteDacRequest {
    private Integer dacId;
    private Integer dacVer;
    private Integer societeId;
    private MethodePaiement methodePaiement = MethodePaiement.MANUEL;
    private Integer justifPaiementId;
    private MultipartFile justifPaiementFile;
}
