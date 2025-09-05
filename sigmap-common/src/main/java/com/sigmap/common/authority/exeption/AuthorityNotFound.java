package com.sigmap.common.authority.exeption;

import lombok.Getter;

import java.util.List;

@Getter
public class AuthorityNotFound extends RuntimeException{
    private final List<String> authority;
    public AuthorityNotFound(String message, List<String> authority) {
        super(message);
        this.authority = authority;
    }
}
