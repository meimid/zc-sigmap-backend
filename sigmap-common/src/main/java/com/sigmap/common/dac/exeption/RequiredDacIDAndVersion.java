package com.sigmap.common.dac.exeption;

public class RequiredDacIDAndVersion extends RuntimeException {

    public RequiredDacIDAndVersion() {
        super("Dac ID and Version is required");
    }
    public RequiredDacIDAndVersion(String message) {
        super(message);
    }
}
