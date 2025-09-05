package com.sigmap.common.reunionDac;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion_dac_doc.ReunionDacDoc;
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
@Table(name = "reunion_dac")
@IdClass(ReunionDacId.class)
public class ReunionDac {

    @Id
    @Column(name = "reunion_id")
    private Integer reunionId;

    @Id
    @Column(name = "dac_id")
    private Integer dacId;

    @Id
    @Column(name = "dac_ver")
    private Integer dacVer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reunion_id", insertable = false, updatable = false)
    private Reunion reunion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id", insertable = false, updatable = false),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver", insertable = false, updatable = false)
    })
    private Dac dac;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "task_id")
    private String taskId;

    private String decision;

    private String observation;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "reunionDac", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReunionDacDoc> reunionDacDocs = new ArrayList<>();

}
