package com.sigmap.common.dyna_fields_details;

import com.sigmap.common.dyna_fields.DynaFields;
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
@Table(name = "dyna_fields_details")
@Entity
@IdClass(DynaFieldsDetailsId.class)
public class DynaFieldsDetails {

  @Id
  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dyna_fields_details_id_seq")
  // @SequenceGenerator(
  //     name = "dyna_fields_details_id_seq",
  //     sequenceName = "dyna_fields_details_id_seq",
  //     allocationSize = 1)
  @Column(name = "dyna_fields_details_id")
  private Integer dynaFieldsDetailsId;

  @Id
  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Id
  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;

  @Id
  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;

  @Column(name = "column_name", length = 100)
  private String columnName;

  @Column(name = "column_pos")
  private Integer columnPos;

  @ManyToOne
  @JoinColumn(name = "type_id")
  private RefParam typeId;

  @Column(name = "source", length = 100)
  private String source;

  @Column(name = "source_field", length = 100)
  private String source_field;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(
        name = "dyna_fields_id",
        referencedColumnName = "dyna_fields_id",
        updatable = false,
        insertable = false),
    @JoinColumn(
        name = "dyna_docs_id",
        referencedColumnName = "dyna_docs_id",
        updatable = false,
        insertable = false),
    @JoinColumn(
        name = "dyna_docs_ver",
        referencedColumnName = "dyna_docs_ver",
        updatable = false,
        insertable = false)
  })
  private DynaFields dynaFields;
}
