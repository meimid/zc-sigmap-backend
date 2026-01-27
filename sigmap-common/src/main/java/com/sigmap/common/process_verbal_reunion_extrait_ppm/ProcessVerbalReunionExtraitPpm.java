package com.sigmap.common.process_verbal_reunion_extrait_ppm;

import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "process_verbal_reunion_extrait_ppm")
@IdClass(ProcessVerbalReunionExtraitPpmId.class)
public class ProcessVerbalReunionExtraitPpm {

    @Id
    @Column(name = "extrait_id")
    private Integer extraitId;

    @Id
    @Column(name = "pv_id")
    private Integer pvId;

    @Id
    @Column(name = "ac_id")
    private Integer acId;

    @Id
    @Column(name = "ppm_id")
    private Integer ppmId;

    @Id
    @Column(name = "ppm_ver")
    private Integer ppmVer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "extrait_id", referencedColumnName = "extrait_id", insertable = false, updatable = false),
            @JoinColumn(name = "pv_id", referencedColumnName = "pv_id", insertable = false, updatable = false),
            @JoinColumn(name = "ac_id", referencedColumnName = "ac_id", insertable = false, updatable = false)
    })
    private ProcessVerbalReunionExtrait extrait;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false)
    })
    private Ppm ppm;
}