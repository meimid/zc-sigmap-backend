package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.LotsId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "ppm")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
public class Ppm  extends BaseEntity {
    @Id
    @Column(name = "ppm_id")
    private Integer id;

    @Id
    @Column(name = "ppm_ver")
    private Integer version;

    @Column(name = "ref", length = 100)
    private String ref;

    @Column(name = "nom", length = 255)
    private String nom;

    @Column(name = "annee", length = 4)
    private String annee;

    @ManyToOne
    @JoinColumn(name = "ac_id")
    private Ac ac;

    @Column(name = "budget_estime", precision = 10, scale = 2)
    private BigDecimal budgetEstime;

    @Column(name = "status_id")
    private Integer statusId;
}
