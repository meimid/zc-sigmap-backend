package com.sigmap.common.model_paa;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.paas.Paa;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "model_paa")
@IdClass(ModelPaaId.class)
public class ModelPaa extends BaseEntity {

    @Id
    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Id
    @Column(name = "paa_id", nullable = false)
    private Integer paaId;

    @Id
    @Column(name = "paa_ver", nullable = false)
    private Integer paaVer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs doc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "paa_id",  referencedColumnName = "paa_id", insertable = false, updatable = false),
            @JoinColumn(name = "paa_ver", referencedColumnName = "paa_ver", insertable = false, updatable = false)
    })
    private Paa paa;

}
