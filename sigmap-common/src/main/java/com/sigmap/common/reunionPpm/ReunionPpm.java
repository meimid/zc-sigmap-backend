package com.sigmap.common.reunionPpm;

import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reunion_ppm")
@IdClass(ReunionPpmId.class)
@ToString(onlyExplicitlyIncluded = true)
public class ReunionPpm {

    @Id
    @Column(name = "reunion_id")
    private Integer reunionId;
    @Id
    @Column(name = "ppm_id")
    private Integer ppmId;
    @Id
    @Column(name = "ppm_ver")
    private Integer ppmVer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reunion_id", insertable = false, updatable = false)
    private Reunion reunion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false)
    })
    private Ppm ppm;
    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "task_id")
    private String taskId;

    private String decision;

    private String observation;
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "reunionPpm", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReunionPpmDoc> reunionPpmDocs = new ArrayList<>();
}