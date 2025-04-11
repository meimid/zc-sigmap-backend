package com.sigmap.common.lots;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "lots")
@Data
@IdClass(CompositeId.class)
public class Lot extends BaseEntity{
    @Id
    @Column(name = "lots_id")
    private Integer id;

    @Id
    @Column(name = "lots_ver")
    private Integer version;

    @Column(name = "description", length = 100)
    private String description;

    @ManyToMany(mappedBy = "lots")
    private Set<Realisation> realisations;

}