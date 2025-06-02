package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_fields.DynaFields;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DacDocsId implements Serializable {

  @ManyToOne
  @JoinColumns({
    @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
    @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
  })
  private Dac dac;

  @ManyToOne
  @JoinColumns({@JoinColumn(name = "dyna_fields_id", referencedColumnName = "dyna_fields_id")})
  private DynaFields dynaField;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(name = "dyna_docs_id", referencedColumnName = "dyna_docs_id"),
    @JoinColumn(name = "dyna_docs_ver", referencedColumnName = "dyna_docs_ver")
  })
  private DynaDocs dynaDocs;
}
