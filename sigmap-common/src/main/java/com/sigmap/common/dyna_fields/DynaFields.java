package com.sigmap.common.dyna_fields;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.ref_params.RefParam;
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

   // @EmbeddedId
    //private DynaFieldsId id;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dyna_fields_id")
    private Integer dynaFieldsId;

    @Column(name = "name")
    private String name;
    @Column(name = "ref")
    private String ref;
    @Column(name = "label")
    private String label;
    @Column(name = "description")
    private String description;
    @ManyToOne
    @JoinColumn(name = "type_id")
    private RefParam type_id;
    @Column(name = "source")
    private String source;
    @Column(name = "source_field")
    private String sourceField;
    @Column(name = "dyna_fields_order")
    private Integer dynaFieldsOrder;
    @Column(name = "status")
    private Integer status;


    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dyna_docs_id", referencedColumnName = "dyna_docs_id"),
            @JoinColumn(name = "dyna_docs_ver", referencedColumnName = "dyna_docs_ver"),
    })
    private DynaDocs dynaDocs;

}
