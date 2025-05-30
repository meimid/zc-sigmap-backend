package com.sigmap.common.dyna_docs_fields;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dyna_docs_fields")
public class DynaDocsFields {
    @EmbeddedId
    private DynaDocsFieldsId id;
    @Column(name = "dyna_field_order")
    private Integer dynaFieldOrder;
    @Column(name = "status")
    private boolean status;
}
