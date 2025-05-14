package com.sigmap.common.dyna_docs_fields;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_fields.DynaFields;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
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
    @JoinColumn(name = "dyna_docs_id", nullable = false)
    private DynaDocs dynaDocsId;
    @ManyToOne
    @JoinColumn(name = "dyna_fields_id", nullable = false)
    private DynaFields DynaFieldsId;
}
