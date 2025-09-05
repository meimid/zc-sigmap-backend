package com.sigmap.common.reunionPpm;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReunionPpmId implements Serializable {
    private Integer reunionId;
    private Integer ppmId;
    private Integer ppmVer;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReunionPpmId that = (ReunionPpmId) o;
        return Objects.equals(reunionId, that.reunionId)&&
                Objects.equals(ppmId, that.ppmId) &&
                Objects.equals(ppmVer, that.ppmVer) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reunionId, ppmId, ppmVer);
    }
}