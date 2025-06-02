package com.sigmap.common.dyna_fields;

import com.sigmap.common.dyna_docs.DynaDocs;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaFieldsId implements Serializable {

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dyna_docs_id", referencedColumnName = "dyna_docs_id"),
            @JoinColumn(name = "dyna_docs_ver", referencedColumnName = "dyna_docs_ver"),
    })
    private DynaDocs dynaDocs;


    @Column(name = "dyna_fields_id")
    private Integer dynaFieldsId;
}
