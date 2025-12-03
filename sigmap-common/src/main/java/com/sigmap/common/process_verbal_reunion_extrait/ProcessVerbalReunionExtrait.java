package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "process_verbal_reunion_extrait")
@IdClass(ExtraitId.class)
public class ProcessVerbalReunionExtrait {

    @Id
    @Column(name = "extrait_id")
    private Integer extraitId;

    @Id
    @Column(name = "pv_id")
    private Integer pvId;

    @Id
    @Column(name = "ac_id")
    private Integer acId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id"),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver")
    })
    private Ppm ppm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pv_id", insertable = false, updatable = false)
    private ProcessVerbalReunion processVerbal;

    @ManyToOne
    @JoinColumn(name = "ac_id", insertable = false, updatable = false)
    private Ac ac;

    @OneToOne
    @JoinColumn(name = "doc_id")
    private Docs doc;
}