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
public class Lot extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lots_id")
    private Integer lotsId;

    @Id
    @Column(name = "lots_ver")
    private Integer lotsVer;

    @Column(name = "description", length = 100)
    private String description;

    @ManyToMany(mappedBy = "lots")
    private Set<Realisation> realisations;

}