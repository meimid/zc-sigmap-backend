package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion_extrait_dac.ProcessVerbalReunionExtraitDac;
import com.sigmap.common.process_verbal_reunion_extrait_ppm.ProcessVerbalReunionExtraitPpm;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    @JoinColumn(name = "pv_id", insertable = false, updatable = false)
    private ProcessVerbalReunion processVerbal;

    @ManyToOne
    @JoinColumn(name = "ac_id", insertable = false, updatable = false)
    private Ac ac;

    @OneToOne
    @JoinColumn(name = "doc_id")
    private Docs doc;
    @Enumerated(EnumType.STRING)
    @Column(name = "extrait_type")
    private ExtraitType extraitType;

    @OneToMany(mappedBy = "extrait", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProcessVerbalReunionExtraitPpm> extraitsPpm = new ArrayList<>();

    @OneToMany(mappedBy = "extrait", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProcessVerbalReunionExtraitDac> extraitsDac = new ArrayList<>();
}