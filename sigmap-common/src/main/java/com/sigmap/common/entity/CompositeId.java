package com.sigmap.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class CompositeId implements Serializable {

    private Integer id;
    private Integer version;

    public CompositeId() {}

    public CompositeId(Integer id, Integer version) {
        this.id = id;
        this.version = version;
    }

}
