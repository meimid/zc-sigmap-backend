package com.sigmap.common.menu_item.exception;

import lombok.Data;

@Data
public class MenuItemNotFound extends Exception {
    private final Integer id;

    public MenuItemNotFound(Integer id, String message) {
        super(message);
        this.id = id;
    }

    public MenuItemNotFound(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}


