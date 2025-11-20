package com.sigmap.common.dac_doc_soumission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DacDocSoumissionId implements Serializable {
    private Integer dacId;
    private Integer dacVer;
    private Integer docSoumissionId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DacDocSoumissionId that = (DacDocSoumissionId) o;
        return Objects.equals(dacId, that.dacId)&&
                Objects.equals(dacVer, that.dacVer) &&
                Objects.equals(docSoumissionId, that.docSoumissionId) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dacId, dacVer, docSoumissionId);
    }
}
