package com.sigmap.common.process_verbal_reunion_extrait_ppm;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class ProcessVerbalReunionExtraitPpmId implements Serializable {

    private Integer extraitId;
    private Integer pvId;
    private Integer acId;

    private Integer ppmId;
    private Integer ppmVer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessVerbalReunionExtraitPpmId that =
                (ProcessVerbalReunionExtraitPpmId) o;

        return Objects.equals(extraitId, that.extraitId)
                && Objects.equals(pvId, that.pvId)
                && Objects.equals(acId, that.acId)
                && Objects.equals(ppmId, that.ppmId)
                && Objects.equals(ppmVer, that.ppmVer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extraitId, pvId, acId, ppmId, ppmVer);
    }
}
