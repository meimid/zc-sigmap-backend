package com.sigmap.common.process_verbal_reunion_extrait_dac;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "process_verbal_reunion_extrait_dac")
@IdClass(ProcessVerbalReunionExtraitDacId.class)
public class ProcessVerbalReunionExtraitDac {

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
    @Column(name = "dac_id")
    private Integer dacId;

    @Id
    @Column(name = "dac_ver")
    private Integer dacVer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "extrait_id", referencedColumnName = "extrait_id", insertable = false, updatable = false),
            @JoinColumn(name = "pv_id", referencedColumnName = "pv_id", insertable = false, updatable = false),
            @JoinColumn(name = "ac_id", referencedColumnName = "ac_id", insertable = false, updatable = false)
    })
    private ProcessVerbalReunionExtrait extrait;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id", insertable = false, updatable = false),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver", insertable = false, updatable = false)
    })
    private Dac dac;
}
