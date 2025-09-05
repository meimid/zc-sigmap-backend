package com.sigmap.common.menu_item.response;

import com.sigmap.common.authority.Authority;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class MenuItemResponse {

    private Integer id;
    private String label;
    private String icon;
    private String route;
    private Integer orderIndex;
    private MenuItemResponseDTO parentId;
    private Authority authority;

    private BaseEntityResponse metadata;
}
