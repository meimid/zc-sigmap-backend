package com.sigmap.common.dac_doc_soumission;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.doc_soumission.DocSoumission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac_doc_soumission")
@IdClass(DacDocSoumissionId.class)

public class DacDocSoumission {


    @Id
    @Column(name = "dac_id")
    private Integer dacId;

    @Id
    @Column(name = "dac_ver")
    private Integer dacVer;

    @Id
    @Column(name = "doc_soumission_id")
    private Integer docSoumissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_soumission_id", insertable = false, updatable = false)
    private DocSoumission docSoumission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id", insertable = false, updatable = false),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver", insertable = false, updatable = false)
    })
    private Dac dac;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_validate")
    private Calendar dateValidate;
}
