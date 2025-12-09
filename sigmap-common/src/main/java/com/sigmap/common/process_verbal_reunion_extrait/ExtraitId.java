package com.sigmap.common.process_verbal_reunion_extrait;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraitId implements Serializable {

    private Integer extraitId;

    private Integer pvId;

    private Integer acId;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtraitId that = (ExtraitId) o;
        return Objects.equals(extraitId, that.extraitId) &&
                Objects.equals(pvId, that.pvId) &&
                Objects.equals(acId, that.acId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extraitId, pvId, acId);
    }

}