package com.sigmap.common.dac;

import java.io.Serializable;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DacId implements Serializable {

  private Integer dacId;

  private Integer dacVer;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DacId dacId = (DacId) o;
    return Objects.equals(this.dacId, dacId.dacId) && Objects.equals(this.dacVer, dacId.dacVer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dacId, dacVer);
  }
}
