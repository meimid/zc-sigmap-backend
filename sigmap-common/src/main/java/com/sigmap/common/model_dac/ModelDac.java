package com.sigmap.common.model_dac;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "model_dac")
@IdClass(ModelDacId.class)
public class ModelDac extends BaseEntity {

    @Id
    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Id
    @Column(name = "dac_id", nullable = false)
    private Integer dacId;

    @Id
    @Column(name = "dac_ver", nullable = false)
    private Integer dacVer;

    @Column(name = "is_aao", nullable = false)
    private Boolean isAao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs doc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "dac_id",  referencedColumnName = "dac_id", insertable = false, updatable = false),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver", insertable = false, updatable = false)
    })
    private Dac dac;
}
