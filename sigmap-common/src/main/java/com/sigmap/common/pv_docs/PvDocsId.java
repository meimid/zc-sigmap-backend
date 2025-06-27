package com.sigmap.common.pv_docs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.Objects;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PvDocsId implements Serializable {
    private Integer ppmId;
    private Integer ppmVersion;
    private String processVerbalId;
    private Integer docId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PvDocsId that = (PvDocsId) o;
        return Objects.equals(ppmId, that.ppmId) &&
                Objects.equals(ppmVersion, that.ppmVersion) &&
                Objects.equals(processVerbalId, that.processVerbalId) &&
                Objects.equals(docId, that.docId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ppmId, ppmVersion, processVerbalId, docId);
    }
}
