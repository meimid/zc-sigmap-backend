package com.sigmap.common.model_ppm;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ppms.Ppm;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "model_ppm")
@IdClass(ModelPpmId.class)
public class ModelPpm extends BaseEntity {

    @Id
    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Id
    @Column(name = "ppm_id", nullable = false)
    private Integer ppmId;

    @Id
    @Column(name = "ppm_ver", nullable = false)
    private Integer ppmVer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs doc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id",  referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false)
    })
    private Ppm ppm;

}
