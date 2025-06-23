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
    // These field names must match the field names in the PvDocs entity
    private Integer ppmId;
    private Integer ppmVersion;
    private String processVerbalId;
    private Integer docId; // Changed from 'id' to match the field name in PvDocs entity

    // equals() and hashCode() are mandatory for ID classes
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
