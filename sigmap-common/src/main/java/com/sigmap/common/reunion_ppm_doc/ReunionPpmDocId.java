package com.sigmap.common.reunion_ppm_doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReunionPpmDocId implements Serializable {
    private Integer reunionId;
    private Integer ppmId;
    private Integer ppmVer;
    private Integer docId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReunionPpmDocId that = (ReunionPpmDocId) o;
        return Objects.equals(reunionId, that.reunionId) &&
                Objects.equals(ppmId, that.ppmId) &&
                Objects.equals(ppmVer, that.ppmVer) &&
                Objects.equals(docId, that.docId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reunionId, ppmId, ppmVer, docId);
    }
}
