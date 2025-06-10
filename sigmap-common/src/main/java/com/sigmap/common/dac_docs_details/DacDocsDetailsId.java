package com.sigmap.common.dac_docs_details;

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

  private Integer dacId;

  private Integer dacVer;

  private Integer dynaDocsId;

  private Integer dynaDocsVer;

  private Integer dynaFieldsId;

  private Integer dynaFieldsDetailsId;

  private Integer rowId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DacDocsDetailsId that = (DacDocsDetailsId) o;
    return Objects.equals(dacId, that.dacId)
        && Objects.equals(dacVer, that.dacVer)
        && Objects.equals(dynaDocsId, that.dynaDocsId)
        && Objects.equals(dynaDocsVer, that.dynaDocsVer)
        && Objects.equals(dynaFieldsId, that.dynaFieldsId)
        && Objects.equals(dynaFieldsDetailsId, that.dynaFieldsDetailsId)
        && Objects.equals(rowId, that.rowId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId, dynaFieldsDetailsId, rowId);
  }
}
