package com.sigmap.common.dac_docs_details;

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
public class DacDocsDetailsId implements Serializable {

  @Column(name = "dac_id")
  private Integer dacId;

  @Column(name = "dac_ver")
  private Integer dacVer;

  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;

  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;

  @Column(name = "dyna_fields_details_id")
  private Integer dynaFieldsDetailsId;

  @Column(name = "row_id")
  private Integer rowId;
}
