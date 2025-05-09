package com.sigmap.common.realisations.exceptions;

import lombok.Getter;

@Getter
public class RealisationsMinimumRequirement extends Exception {
    public RealisationsMinimumRequirement(String message) {
        super(message);

    }

}
