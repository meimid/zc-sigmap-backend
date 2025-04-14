package com.sigmap.common.lots;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;

@Entity
@Table(name = "lots")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
@EntityListeners(AuditingEntityListener.class)
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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonBackReference
    private Set<Realisation> realisations;

}