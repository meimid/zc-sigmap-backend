package com.sigmap.common.dac_docs_details;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DacDocsDetailsId that = (DacDocsDetailsId) o;
    return Objects.equals(dacId, that.dacId) &&
            Objects.equals(dacVer, that.dacVer) &&
            Objects.equals(dynaDocsId, that.dynaDocsId) &&
            Objects.equals(dynaDocsVer, that.dynaDocsVer) &&
            Objects.equals(dynaFieldsId, that.dynaFieldsId) &&
            Objects.equals(dynaFieldsDetailsId, that.dynaFieldsDetailsId) &&
            Objects.equals(rowId, that.rowId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId, dynaFieldsDetailsId, rowId);
  }
}
