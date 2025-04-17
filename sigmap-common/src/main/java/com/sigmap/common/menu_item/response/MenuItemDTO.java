package com.sigmap.common.menu_item.response;

import lombok.Data;

@Data
public class MenuItemDTO {
    private Integer id;
    private String label;
    private String icon;
    private String route;
    private Integer orderIndex;

}
