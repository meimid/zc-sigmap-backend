package com.sigmap.common.reunion_ppm_doc;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunionPpm.ReunionPpm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reunion_ppm_doc")
@IdClass(ReunionPpmDocId.class)
public class ReunionPpmDoc {
    @Id
    @Column(name = "reunion_id")
    private Integer reunionId;

    @Id
    @Column(name = "ppm_id")
    private Integer ppmId;

    @Id
    @Column(name = "ppm_ver")
    private Integer ppmVer;

    @Id
    @Column(name = "doc_id")
    private Integer docId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reunion_id", referencedColumnName = "reunion_id", insertable = false, updatable = false)
    private Reunion reunion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false)
    })
    private Ppm ppm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs docs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "reunion_id", referencedColumnName = "reunion_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false)
    })
    private ReunionPpm reunionPpm;
}
