package com.sigmap.common.model_agpm;

import java.io.Serializable;
import java.util.Objects;

public class ModelAgpmId implements Serializable {
    private Integer docId;
    private Integer agpmId;
    private Integer agpmVer;

    public ModelAgpmId() {}

    public ModelAgpmId(Integer docId, Integer agpmId, Integer agpmVer) {
        this.docId = docId;
        this.agpmId = agpmId;
        this.agpmVer = agpmVer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelAgpmId)) return false;
        ModelAgpmId that = (ModelAgpmId) o;
        return Objects.equals(docId, that.docId)
                && Objects.equals(agpmId, that.agpmId)
                && Objects.equals(agpmVer, that.agpmVer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId, agpmId, agpmVer);
    }
}
