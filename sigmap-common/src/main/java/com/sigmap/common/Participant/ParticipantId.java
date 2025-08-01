package com.sigmap.common.Participant;


import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
@Data
public class ParticipantId implements Serializable {
    private Integer id;
    private Integer reunionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantId that = (ParticipantId) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(reunionId, that.reunionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reunionId);
    }
}
