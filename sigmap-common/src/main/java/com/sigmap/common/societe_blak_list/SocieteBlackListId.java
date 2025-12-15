package com.sigmap.common.societe_blak_list;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocieteBlackListId implements Serializable {


    private Integer id;

    private Integer societeId;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocieteBlackListId that = (SocieteBlackListId) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(societeId, that.societeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, societeId);
    }
}
