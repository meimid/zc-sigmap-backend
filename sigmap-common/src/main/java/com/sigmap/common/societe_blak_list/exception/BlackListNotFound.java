package com.sigmap.common.societe_blak_list.exception;

import lombok.Getter;

@Getter
public class BlackListNotFound extends RuntimeException {
    private Integer id;
    public BlackListNotFound(String message, Integer id) {
        super(message);
        this.id = id;
    }
}