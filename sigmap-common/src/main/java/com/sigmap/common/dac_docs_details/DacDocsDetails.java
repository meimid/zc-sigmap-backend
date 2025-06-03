package com.sigmap.common.dac_docs_details;

import com.sigmap.common.dac_docs.DacDocs;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac_docs_details")
@IdClass(DacDocsDetailsId.class)
public class DacDocsDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dyna_fields_details_id")
  private Integer dynaFieldsDetailsId;

  @Id
  @Column(name = "row_id")
  private Integer rowId;

  @Id
  @Column(name = "dac_id")
  private Integer dacId;

  @Id
  @Column(name = "dac_ver")
  private Integer dacVer;

  @Id
  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Id
  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;

  @Id
  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;

  @Column(name = "source_id", length = 100)
  private String sourceId;

  @Column(name = "field_value", length = 255)
  private String fieldValue;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(
        name = "dac_id",
        referencedColumnName = "dac_id",
        updatable = false,
        insertable = false),
    @JoinColumn(
        name = "dac_ver",
        referencedColumnName = "dac_ver",
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
        insertable = false),
    @JoinColumn(
        name = "dyna_fields_id",
        referencedColumnName = "dyna_fields_id",
        updatable = false,
        insertable = false),
  })
  private DacDocs dac;
}
