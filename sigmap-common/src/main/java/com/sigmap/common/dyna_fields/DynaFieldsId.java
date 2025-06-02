package com.sigmap.common.dyna_fields;

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
public class DynaFieldsId implements Serializable {

  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;

  // @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;
}
