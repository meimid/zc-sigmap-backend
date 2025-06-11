package com.sigmap.common.menu_item.response;

import lombok.Data;


@Data
public class MenuItemResponseDTO {
    private Integer id;
    private String label;
    private String icon;
    private String route;
    private Integer orderIndex;

}
