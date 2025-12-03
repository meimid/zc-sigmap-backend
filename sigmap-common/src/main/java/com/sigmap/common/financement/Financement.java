package com.sigmap.common.financement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "finacement")
@Data
@NoArgsConstructor
public class Financement extends BaseEntity {

    @EmbeddedId
    private FinancementId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("realisation")
    @JoinColumns({
            @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
            @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
    })
    @JsonBackReference("realisation-financements")
    private Realisation realisation;

    @ManyToOne
    @JoinColumn(name = "monnaie_id")
    private Monnaie monnaie;

    @Column(name = "taux_applicable")
    private Double tauxApplicable;

    @Column(name = "budget_estimer", precision = 20, scale = 3)
    private BigDecimal budgetEstimer;

    @ManyToOne
    @JoinColumn(name = "source_de_financement_id")
    private RefParam sourceFinancement;
}
