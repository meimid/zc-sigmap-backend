package com.sigmap.common.process_verbal_reunion_extrait_dac;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProcessVerbalReunionExtraitDacId implements Serializable {

    private Integer extraitId;
    private Integer pvId;
    private Integer acId;

    private Integer dacId;
    private Integer dacVer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessVerbalReunionExtraitDacId that =
                (ProcessVerbalReunionExtraitDacId) o;

        return Objects.equals(extraitId, that.extraitId)
                && Objects.equals(pvId, that.pvId)
                && Objects.equals(acId, that.acId)
                && Objects.equals(dacId, that.dacId)
                && Objects.equals(dacVer, that.dacVer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extraitId, pvId, acId, dacId, dacVer);
    }
}