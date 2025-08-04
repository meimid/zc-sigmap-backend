package com.sigmap.common.reunion;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.com.Com;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.entity.BaseEntity;
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
    private String object;

    @Enumerated(EnumType.STRING)
    private ReunionStatus status;

    @OneToMany(fetch = FetchType.EAGER,
            mappedBy = "reunion"
    )

    private List<ReunionPpm> reunionPpms ;

    @OneToMany(fetch = FetchType.EAGER,


            mappedBy = "reunion"
    )

    private List<ReunionDac> reunionDacs = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER,

            mappedBy = "reunion"
    )

    private List<Participant> participants = new ArrayList<>();
}
