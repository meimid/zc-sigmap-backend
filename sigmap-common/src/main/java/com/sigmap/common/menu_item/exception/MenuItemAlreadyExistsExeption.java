package com.sigmap.common.menu_item.exception;

import lombok.Data;

@Data
public class MenuItemAlreadyExistsExeption extends Exception {
    private final Integer id;
    public MenuItemAlreadyExistsExeption(Integer id, String message) {
        super(message);
        this.id = id;
    }
    public MenuItemAlreadyExistsExeption(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
