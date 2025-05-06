package com.sigmap.common.menu_item.exception;

import lombok.Getter;

@Getter
public class MenuItemUpdateExeption extends RuntimeException {
    private final Integer id;
    public MenuItemUpdateExeption(String message, Integer id) {
        super(message);
        this.id = id;
    }
}
