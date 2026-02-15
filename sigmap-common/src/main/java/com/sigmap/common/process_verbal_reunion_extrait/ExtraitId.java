package com.sigmap.common.process_verbal_reunion_extrait;


import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

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