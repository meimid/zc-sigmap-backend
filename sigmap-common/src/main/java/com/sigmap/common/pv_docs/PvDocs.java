package com.sigmap.common.pv_docs;


import lombok.Data;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.process_verbal.ProcessVerbal;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
@Data
@Entity
@Table(name = "pv_docs")
@IdClass(PvDocsId.class)
public class PvDocs {

    @Id
    @Column(name = "ppm_id")
    private Integer ppmId;

    @Id
    @Column(name = "ppm_ver")
    private Integer ppmVersion;

    @Id
    @Column(name = "process_verbal_id")
    private String processVerbalId;


    @Id
    @Column(name = "doc_id")
    private Integer docId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false),
            @JoinColumn(name = "process_verbal_id", referencedColumnName = "process_verbal_id", insertable = false, updatable = false)
    })
    private ProcessVerbal processVerbal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs doc;
    private boolean pv;

}
