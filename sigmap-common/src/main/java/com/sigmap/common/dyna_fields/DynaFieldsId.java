package com.sigmap.common.dyna_fields;

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
public class DynaFieldsId implements Serializable {

  private Integer dynaDocsId;

  private Integer dynaDocsVer;

  private Integer dynaFieldsId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynaFieldsId that = (DynaFieldsId) o;
    return Objects.equals(dynaDocsId, that.dynaDocsId)
        && Objects.equals(dynaDocsVer, that.dynaDocsVer)
        && Objects.equals(dynaFieldsId, that.dynaFieldsId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dynaDocsId, dynaDocsVer, dynaFieldsId);
  }
}
