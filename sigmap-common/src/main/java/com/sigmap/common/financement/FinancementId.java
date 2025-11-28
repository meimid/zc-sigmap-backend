package com.sigmap.common.financement;

import com.sigmap.common.entity.CompositeId;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class FinancementId implements Serializable {

    @Column(name = "realisation_id")
    private Integer realisationId;

    @Column(name = "realisation_ver")
    private Integer realisationVer;

    @Column(name = "financement_id")
    private Integer financementId;

    /**
     * Virtual ID
     * NOT persisted in the database.
     */
    @Transient
    private CompositeId realisation;


    public FinancementId(Integer realisationId, Integer realisationVer, Integer financementId) {
        this.realisationId = realisationId;
        this.realisationVer = realisationVer;
        this.financementId = financementId;
        // Construct virtual CompositeId automatically
        this.realisation = new CompositeId(realisationId, realisationVer);
    }


    public FinancementId(Integer realisationId,
                         Integer realisationVer,
                         Integer financementId,
                         CompositeId realisation) {
        this.realisationId = realisationId;
        this.realisationVer = realisationVer;
        this.financementId = financementId;
        this.realisation = realisation;
    }
}
