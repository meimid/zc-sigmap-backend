package com.sigmap.common.menu_item.request;

import lombok.Data;

@Data
public class MenuItemRequest {
    private Integer id;
    private String label;
    private String icon;
    private String route;
    private Integer orderIndex;
    private Integer parentId;
    private String authority;
}
