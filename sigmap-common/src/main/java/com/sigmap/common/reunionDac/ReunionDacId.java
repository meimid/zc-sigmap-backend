package com.sigmap.common.reunionDac;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReunionDacId implements Serializable {
    private Integer reunionId;
    private Integer dacId;
    private Integer dacVer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReunionDacId that = (ReunionDacId) o;
        return Objects.equals(reunionId, that.reunionId) &&
                Objects.equals(dacId, that.dacId) &&
                Objects.equals(dacVer, that.dacVer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reunionId, dacId, dacVer);
    }
}

