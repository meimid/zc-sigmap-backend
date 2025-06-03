package com.sigmap.common.dac;

import jakarta.persistence.Column;
import java.io.Serializable;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DacId implements Serializable {

  @Column(name = "dac_id")
  private Integer dacId;

  @Column(name = "dac_ver")
  private Integer dacVer;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DacId dacId = (DacId) o;
    return Objects.equals(this.dacId, dacId.dacId) &&
            Objects.equals(this.dacVer, dacId.dacVer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dacId, dacVer);
  }
}
