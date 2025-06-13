package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dyna_fields.DynaFields;
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
@Table(name = "dac_docs")
public class DacDocs {

  @EmbeddedId private DacDocsId id;

  @Column(name = "source_id")
  private String sourceId;

  @Column(name = "field_value", length = 255)
  private String fieldValue;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(
        name = "dac_id",
        referencedColumnName = "dac_id",
        insertable = false,
        updatable = false),
    @JoinColumn(
        name = "dac_ver",
        referencedColumnName = "dac_ver",
        insertable = false,
        updatable = false)
  })
  private Dac dac;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(
        name = "dyna_fields_id",
        referencedColumnName = "dyna_fields_id",
        insertable = false,
        updatable = false),
    @JoinColumn(
        name = "dyna_docs_id",
        referencedColumnName = "dyna_docs_id",
        insertable = false,
        updatable = false),
    @JoinColumn(
        name = "dyna_docs_ver",
        referencedColumnName = "dyna_docs_ver",
        insertable = false,
        updatable = false)
  })
  private DynaFields dynaField;
}
