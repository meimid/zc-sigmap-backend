package com.sigmap.common.process_verbal_reunion_doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessVerbalReunionDocId implements Serializable {
    private Integer pvId;
    private Integer docId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessVerbalReunionDocId that = (ProcessVerbalReunionDocId) o;
        return Objects.equals(pvId, that.pvId) &&
                Objects.equals(docId, that.docId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pvId, docId);
    }
}

