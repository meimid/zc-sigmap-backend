package com.sigmap.common.societe_blak_list;

import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocieteBlackListId implements Serializable {


    @Column(name = "balck_list_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "societe_id", nullable = false)
    private Societe societeId;
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
