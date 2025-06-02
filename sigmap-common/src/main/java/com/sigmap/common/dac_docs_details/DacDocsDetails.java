package com.sigmap.common.dac_docs_details;

import com.sigmap.common.dac_docs.DacDocs;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class DacDocsDetails {

  @EmbeddedId private DacDocsDetailsId id;

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
