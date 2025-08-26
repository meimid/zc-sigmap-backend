package com.sigmap.common.reunion_dac_doc;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunionDac.ReunionDac;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reunion_dac_doc")
@IdClass(ReunionDacDocId.class)
public class ReunionDacDoc  {
    @Id
    @Column(name = "reunion_id")
    private Integer reunionId;

    @Id
    @Column(name = "dac_id")
    private Integer dacId;

    @Id
    @Column(name = "dac_ver")
    private Integer dacVer;

    @Id
    @Column(name = "doc_id")
    private Integer docId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reunion_id", referencedColumnName = "reunion_id", insertable = false, updatable = false)
    private Reunion reunion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id", insertable = false, updatable = false),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver", insertable = false, updatable = false)
    })
    private Dac dac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs doc;
}