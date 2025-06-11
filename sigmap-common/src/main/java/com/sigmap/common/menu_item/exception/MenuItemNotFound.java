package com.sigmap.common.menu_item.exception;

import lombok.Data;

import java.util.List;

@Data
public class MenuItemNotFound extends Exception {
    private final List<Integer> id;

    public MenuItemNotFound(String message, List<Integer> id) {
        super(message);
        this.id = id;
    }
}


