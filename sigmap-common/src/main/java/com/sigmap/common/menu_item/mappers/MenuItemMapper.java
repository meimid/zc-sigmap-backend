package com.sigmap.common.menu_item.mappers;

import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.request.MenuItemRequest;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {
    MenuItemMapper INSTANCE = Mappers.getMapper(MenuItemMapper.class);
    @Mapping(target = "parentId", source = "parentId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    MenuItemResponse map(MenuItem source);

    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "authority.authorityCode", source = "authority")
    MenuItem toEntity(MenuItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "authority", ignore = true)
    void updateMenuItemFromRequest(MenuItemRequest request, @MappingTarget MenuItem menuItem);
}
