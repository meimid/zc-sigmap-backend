package com.sigmap.common.pv_membre_present;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PvMembrePresentId implements Serializable {
    private Integer ppmId;
    private Integer ppmVersion;
    private String processVerbalId;
    private Integer pvMembrePresentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PvMembrePresentId that = (PvMembrePresentId) o;
        return Objects.equals(ppmId, that.ppmId) &&
                Objects.equals(ppmVersion, that.ppmVersion) &&
                Objects.equals(processVerbalId, that.processVerbalId) &&
                Objects.equals(pvMembrePresentId, that.pvMembrePresentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ppmId, ppmVersion, processVerbalId, pvMembrePresentId);
    }
}