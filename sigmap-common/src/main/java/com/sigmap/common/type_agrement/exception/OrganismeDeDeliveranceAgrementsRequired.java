package com.sigmap.common.type_agrement.exception;

import lombok.Getter;

@Getter
public class OrganismeDeDeliveranceAgrementsRequired extends RuntimeException {

    public OrganismeDeDeliveranceAgrementsRequired(String message) {
        super(message);
    }
}
