package com.sigmap.common.process_verbal;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_membre_present.PvMembrePresent;
import com.sigmap.common.ref_params.RefParam;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_verbal")
@IdClass(ProcessVerbalId.class)
public class ProcessVerbal extends BaseEntity {
    @Id
    @Column(name = "ppm_id")
    private Integer ppmId;

    @Id
    @Column(name = "ppm_ver")
    private Integer ppmVersion;

    @Id
    @Column(name = "process_verbal_id")
    private String processVerbalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false)
    })
    private Ppm ppm;

    @OneToMany(mappedBy = "processVerbal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PvMembrePresent> membres=new HashSet<>();

    @Column(name = "date_debut_reunion")
    private LocalDateTime dateDebutReunion;

    @Column(name = "observation")
    private String observation;

    @Column(name = "decision_id")
    private boolean decision;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private RefParam typeId;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;
    @OneToMany(
            mappedBy = "processVerbal",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<PvDocs> documents;


}

