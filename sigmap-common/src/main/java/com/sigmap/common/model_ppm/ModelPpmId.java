package com.sigmap.common.model_ppm;

import java.io.Serializable;
import java.util.Objects;

public class ModelPpmId implements Serializable {
    private Integer docId;
    private Integer ppmId;
    private Integer ppmVer;

    public ModelPpmId() {}

    public ModelPpmId(Integer docId, Integer ppmId, Integer ppmVer) {
        this.docId = docId;
        this.ppmId = ppmId;
        this.ppmVer = ppmVer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelPpmId)) return false;
        ModelPpmId that = (ModelPpmId) o;
        return Objects.equals(docId, that.docId)
                && Objects.equals(ppmId, that.ppmId)
                && Objects.equals(ppmVer, that.ppmVer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId, ppmId, ppmVer);
    }
}
