package com.sigmap.common.societe_dac.request;

import com.sigmap.common.entity.MethodePaiement;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SocieteDacRequestForCreate {
    private Integer dacId;
    private MethodePaiement methodePaiement = MethodePaiement.MANUEL;
    private MultipartFile justifPaiementFile;
}
