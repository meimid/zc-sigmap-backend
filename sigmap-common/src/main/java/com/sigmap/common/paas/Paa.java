package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "paa")
@Data
@EqualsAndHashCode(callSuper = true)
public class Paa extends BaseEntity {
    @Id
    @Column(name = "paa_id")
    private Integer paaId;

    @Id
    @Column(name = "paa_ver")
    private Integer paaVer;

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

    @ManyToMany(mappedBy = "paas")
    private List<Realisation> realisations;

}
