package com.sigmap.common.model_agpm;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.model_paa.ModelPaaId;
import com.sigmap.common.paas.Paa;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "model_agpm")
@IdClass(ModelAgpmId.class)
public class ModelAgpm extends BaseEntity {

    @Id
    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Id
    @Column(name = "agpm_id", nullable = false)
    private Integer agpmId;

    @Id
    @Column(name = "agpm_ver", nullable = false)
    private Integer agpmVer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs doc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "agpm_id",  referencedColumnName = "agpm_id", insertable = false, updatable = false),
            @JoinColumn(name = "agpm_ver", referencedColumnName = "agpm_ver", insertable = false, updatable = false)
    })
    private Agpm agpm;

}
