package com.sigmap.common.societe_dac.request;

import com.sigmap.common.entity.MethodePaiement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

@Data
@EqualsAndHashCode
public class SocieteDacRequestForWebsite {
    private Integer dacId;
    private MultipartFile justifPaiementFile;
}
