package com.sigmap.common.dyna_fields;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
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
@IdClass(DynaFieldsId.class)
public class DynaFields {

  @Id
  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dyna_fields_id_seq")
  // @SequenceGenerator(
  //     name = "dyna_fields_id_seq",
  //     sequenceName = "dyna_fields_id_seq",
  //     allocationSize = 1)
  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;

  @Id
  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Id
  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;

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
    @JoinColumn(
        name = "dyna_docs_id",
        referencedColumnName = "dyna_docs_id",
        insertable = false,
        updatable = false),
    @JoinColumn(
        name = "dyna_docs_ver",
        referencedColumnName = "dyna_docs_ver",
        insertable = false,
        updatable = false),
  })
  private DynaDocs dynaDocs;

  @OneToMany(mappedBy = "dynaFields")
  private Set<DynaFieldsDetails> dynaFieldsDetails = new HashSet<>();
}
