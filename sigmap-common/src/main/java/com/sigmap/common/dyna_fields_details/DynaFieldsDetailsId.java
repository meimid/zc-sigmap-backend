package com.sigmap.common.dyna_fields_details;

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
public class DynaFieldsDetailsId implements Serializable {

  private Integer dynaFieldsDetailsId;

  private Integer dynaDocsId;

  private Integer dynaDocsVer;

  private Integer dynaFieldsId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynaFieldsDetailsId that = (DynaFieldsDetailsId) o;
    return Objects.equals(dynaFieldsDetailsId, that.dynaFieldsDetailsId)
        && Objects.equals(dynaDocsId, that.dynaDocsId)
        && Objects.equals(dynaDocsVer, that.dynaDocsVer)
        && Objects.equals(dynaFieldsId, that.dynaFieldsId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dynaFieldsDetailsId, dynaDocsId, dynaDocsVer, dynaFieldsId);
  }
}
