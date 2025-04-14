package com.sigmap.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Objects;

@Data
@EqualsAndHashCode()
public class CompositeId implements Serializable {

    private Integer id;
    private Integer version;

    public CompositeId() {}

    public CompositeId(Integer id, Integer version) {
        this.id = id;
        this.version = version;
    }

    // Explicitly implement equals and hashCode for clarity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeId that = (CompositeId) o;
        return Objects.equals(id, that.id) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

}
