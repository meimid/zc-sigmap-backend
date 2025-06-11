package com.sigmap.common.menu_item.exception;

import lombok.Getter;

@Getter
public class MenuItemSoftDeleteExeption extends RuntimeException {
    private final Integer number;
    private final Integer id;

    public MenuItemSoftDeleteExeption(String message, Integer number, Integer id) {
        super(message);
        this.number = number;
        this.id = id;
    }
}
