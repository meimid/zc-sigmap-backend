package com.sigmap.sigmap_agpm.agpm.request;

import lombok.Data;

@Data
public class ValidationRequest {
    private Boolean validate;
    private String comment;
}