package com.sigmap.common.financement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancementId implements Serializable {
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
            @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
    })
    @JsonBackReference("realisation-financements")
    @JsonIgnore
    private Realisation realisation;


    @Column(name = "financement_id")
    private Integer financementId;
}