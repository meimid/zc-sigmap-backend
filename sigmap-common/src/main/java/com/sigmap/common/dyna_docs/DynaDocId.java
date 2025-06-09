package com.sigmap.common.dyna_docs;

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
public class DynaDocId implements Serializable {
  private Integer dynaDocsId;

  private Integer dynaDocsVer;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynaDocId dacId = (DynaDocId) o;
    return Objects.equals(this.dynaDocsId, dacId.dynaDocsId)
        && Objects.equals(this.dynaDocsVer, dacId.dynaDocsVer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dynaDocsId, dynaDocsVer);
  }
}
