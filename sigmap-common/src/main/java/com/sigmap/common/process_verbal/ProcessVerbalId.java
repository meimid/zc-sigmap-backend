package com.sigmap.common.process_verbal;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class ProcessVerbalId implements Serializable {
    private Integer ppmId;
    private Integer ppmVersion;
    private String processVerbalId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessVerbalId that = (ProcessVerbalId) o;
        return Objects.equals(ppmId, that.ppmId) &&
                Objects.equals(ppmVersion, that.ppmVersion) &&
                Objects.equals(processVerbalId, that.processVerbalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ppmId, ppmVersion, processVerbalId);
    }
}