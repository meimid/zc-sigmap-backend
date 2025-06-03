package com.sigmap.common.dyna_docs;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaDocID implements Serializable {
    @Column(name = "dyna_docs_id")
    private Integer dynaDocsId;

    @Column(name = "dyna_docs_ver")
    private Integer dynaDocsVer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynaDocID dacId = (DynaDocID) o;
        return Objects.equals(this.dynaDocsId, dacId.dynaDocsId) &&
                Objects.equals(this.dynaDocsVer, dacId.dynaDocsVer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dynaDocsId, dynaDocsVer);
    }
}
