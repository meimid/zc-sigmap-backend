package com.sigmap.common.reunion;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.com.Com;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ppms.Ppm;
import jakarta.persistence.*;
import lombok.*;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reunion")
public class Reunion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reunion_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "com_id", nullable = false)
    private Com com;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reunion_date")
    private Calendar reunionDate;

    private String emplacement;
    private String object;

    @Enumerated(EnumType.STRING)
    private ReunionStatus status;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "reunion_ppm",
            joinColumns = @JoinColumn(name = "reunion_id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id"),
                    @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver")
            }
    )
    private Set<Ppm> ppms = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "reunion_dac",
            joinColumns = @JoinColumn(name = "reunion_id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
                    @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
            }
    )
    private Set<Dac> dacs = new HashSet<>();



}
