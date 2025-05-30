package com.sigmap.common.dac_docs_details;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dyna_docs_fields.DynaDocsFields;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DacDocsDetailsId {
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
    })
    private Dac dac;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dyna_docs_id", referencedColumnName = "dyna_docs_id"),
            @JoinColumn(name = "dyna_docs_ver", referencedColumnName = "dyna_docs_ver"),
            @JoinColumn(name = "dyna_fields_id", referencedColumnName = "dyna_fields_id")
    })
    private DynaDocsFields dinaDocsFields;

    @Column(name = "dyna_fileds_details_id")
    private Integer dynaFiledsDetailsId;

    @Column(name = "row_id")
    private Integer rowId;

}
