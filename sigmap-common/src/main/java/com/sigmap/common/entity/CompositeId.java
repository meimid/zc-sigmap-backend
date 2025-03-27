package com.sigmap.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class CompositeId<T, V> implements Serializable {

    private T id;
    private V version;

    public CompositeId() {}

    public CompositeId(T id, V version) {
        this.id = id;
        this.version = version;
    }

}
