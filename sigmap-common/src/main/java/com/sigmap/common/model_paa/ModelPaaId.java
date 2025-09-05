package com.sigmap.common.model_paa;

import java.io.Serializable;
import java.util.Objects;

public class ModelPaaId implements Serializable {
    private Integer docId;
    private Integer paaId;
    private Integer paaVer;

    public ModelPaaId() {}

    public ModelPaaId(Integer docId, Integer paaId, Integer paaVer) {
        this.docId = docId;
        this.paaId = paaId;
        this.paaVer = paaVer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelPaaId)) return false;
        ModelPaaId that = (ModelPaaId) o;
        return Objects.equals(docId, that.docId)
                && Objects.equals(paaId, that.paaId)
                && Objects.equals(paaVer, that.paaVer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId, paaId, paaVer);
    }
}
