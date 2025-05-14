package com.sigmap.common.dyna_fields;

import com.sigmap.common.dyna_docs_fields.DynaDocsFields;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dyna_fields")
@Entity
public class DynaFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dyna_fields_id")
    private Integer dynaFieldId;

    @Column(name = "name")
    private String name;
    @Column(name = "ref")
    private String ref;
    @Column(name = "label")
    private String label;
    @Column(name = "description")
    private String description;
    @Column(name = "type_id")
    private Integer type_id;

}
