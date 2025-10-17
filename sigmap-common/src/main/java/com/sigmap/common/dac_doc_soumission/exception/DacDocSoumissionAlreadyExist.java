package com.sigmap.common.dac_doc_soumission.exception;

import lombok.Getter;

@Getter
public class DacDocSoumissionAlreadyExist extends RuntimeException {
    private Integer dacId;
    private Integer dacVersion;
    private Integer docSoumissionId;
    public DacDocSoumissionAlreadyExist(String message,Integer dacId,Integer dacVersion,Integer docSoumissionId) {
        super(message);
        this.dacId = dacId;
        this.dacVersion = dacVersion;
        this.docSoumissionId = docSoumissionId;
    }
}
