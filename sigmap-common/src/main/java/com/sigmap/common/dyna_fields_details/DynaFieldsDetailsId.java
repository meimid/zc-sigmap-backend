package com.sigmap.common.dyna_fields_details;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaFieldsDetailsId {

  @Column(name = "dyna_fields_details_id")
  private Integer dynaFieldsDetailsId;

  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;

  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;
}
