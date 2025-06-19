package com.sigmap.common.direction;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Objects;


@Data
@EqualsAndHashCode()
public class DirectionId implements Serializable {


    private Integer id;
    private Integer acId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectionId that = (DirectionId) o;
        return Objects.equals(id, that.id) && Objects.equals(acId, that.acId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, acId);
    }
}
