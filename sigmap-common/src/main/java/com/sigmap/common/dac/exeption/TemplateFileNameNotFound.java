package com.sigmap.common.dac.exeption;

import lombok.Getter;

@Getter
public class TemplateFileNameNotFound extends RuntimeException {
    private final Integer dacId;
    private final Integer dacVersion;

    public TemplateFileNameNotFound( Integer dacId, Integer dacVersion) {
        super("Template file name not found for dacId: " + dacId + " and dacVersion: " + dacVersion);
        this.dacId = dacId;
        this.dacVersion = dacVersion;
    }
    public TemplateFileNameNotFound(String message, Integer dacId, Integer dacVersion) {
        super(message);
        this.dacId = dacId;
        this.dacVersion = dacVersion;
    }
}
