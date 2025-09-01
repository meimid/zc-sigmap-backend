package com.sigmap.common.process_verbal_reunion_doc;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "process_verbal_reunion_doc")
@IdClass(ProcessVerbalReunionDocId.class)

public class ProcessVerbalReunionDoc {
    @Id
    @Column(name = "pv_id")
    private Integer pvId;
    @Id
    @Column(name = "doc_id")
    private Integer docId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id", insertable = false, updatable = false)
    private Docs doc;
    private boolean pv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pv_id", referencedColumnName = "pv_id", insertable = false, updatable = false)
    private ProcessVerbalReunion processVerbal;
}
