package com.sigmap.common.dyna_docs_fields;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_fields.DynaFields;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaDocsFieldsId implements Serializable {

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dyna_docs_id", referencedColumnName = "dyna_docs_id"),
            @JoinColumn(name = "dyna_docs_ver", referencedColumnName = "dyna_docs_ver")
    })
    private DynaDocs dynaDocsId;


    @ManyToOne
    @JoinColumn(name = "dyna_fields_id", nullable = false)
    private DynaFields DynaFieldsId;
}
