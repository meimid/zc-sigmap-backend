package com.sigmap.common.reunion;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.com.Com;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.reunionDac.ReunionDac;
import com.sigmap.common.reunionPpm.ReunionPpm;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reunion")
public class Reunion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reunion_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "com_id")
    private Com com;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reunion_date")
    private Calendar reunionDate;

    private String emplacement;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String object;

    @Enumerated(EnumType.STRING)
    private ReunionStatus status;

    @OneToMany(
            mappedBy = "reunion"
    )
    @EqualsAndHashCode.Exclude
    private List<ReunionPpm> reunionPpms = new ArrayList<>();

    @OneToMany(
            mappedBy = "reunion"
    )
    @EqualsAndHashCode.Exclude

    private List<ReunionDac> reunionDacs = new ArrayList<>();

    @OneToMany(
            mappedBy = "reunion"
    )
    @EqualsAndHashCode.Exclude
    private List<Participant> participants = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pv_id", referencedColumnName = "pv_id")
    private ProcessVerbalReunion processVerbal;

}
