package com.sigmap.common.dac_docs;

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

  @Column(name = "dac_id")
  private Integer dacId;

  @Column(name = "dac_ver")
  private Integer dacVer;

  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;

  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;
}
