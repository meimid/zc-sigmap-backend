package com.sigmap.common.reunion_dac_doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReunionDacDocId implements Serializable {
    private Integer reunionId;
    private Integer dacId;
    private Integer dacVer;
    private Integer docId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReunionDacDocId that = (ReunionDacDocId) o;
        return Objects.equals(reunionId, that.reunionId) &&
                Objects.equals(dacId, that.dacId) &&
                Objects.equals(dacVer, that.dacVer) &&
                Objects.equals(docId, that.docId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reunionId, dacId, dacVer, docId);
    }
}
