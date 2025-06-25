package com.sigmap.common.process_verbal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_membre_present.PvMembrePresent;
import com.sigmap.common.ref_params.RefParam;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_verbal")
@IdClass(ProcessVerbalId.class)
@ToString(onlyExplicitlyIncluded = true)
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
    @JsonBackReference
    private Ppm ppm;
    @OneToMany(mappedBy = "processVerbal", cascade = CascadeType.ALL)
    //@OneToMany(mappedBy = "processVerbal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PvMembrePresent> membres=new HashSet<>();

    @Column(name = "date_debut_reunion")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
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
    @OneToMany(mappedBy = "processVerbal", cascade = CascadeType.ALL)
    private Set<PvDocs> documents =new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessVerbal that = (ProcessVerbal) o;
        return Objects.equals(ppmId, that.ppmId) &&
                Objects.equals(ppmVersion, that.ppmVersion) &&
                Objects.equals(processVerbalId, that.processVerbalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ppmId, ppmVersion, processVerbalId);
    }
}

