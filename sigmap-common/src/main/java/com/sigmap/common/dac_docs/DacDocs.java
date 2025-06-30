package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

  @OneToMany(mappedBy = "dac")
  private Set<DacDocsDetails> dacDocsDetails = new HashSet<>();
}
