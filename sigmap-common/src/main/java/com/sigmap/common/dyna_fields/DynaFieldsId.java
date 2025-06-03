package com.sigmap.common.dyna_fields;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaFieldsId implements Serializable {

  @Column(name = "dyna_docs_id")
  private Integer dynaDocsId;

  @Column(name = "dyna_docs_ver")
  private Integer dynaDocsVer;

  @Column(name = "dyna_fields_id")
  private Integer dynaFieldsId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynaFieldsId that = (DynaFieldsId) o;
    return Objects.equals(dynaDocsId, that.dynaDocsId) &&
            Objects.equals(dynaDocsVer, that.dynaDocsVer) &&
            Objects.equals(dynaFieldsId, that.dynaFieldsId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dynaDocsId, dynaDocsVer, dynaFieldsId);
  }
}
