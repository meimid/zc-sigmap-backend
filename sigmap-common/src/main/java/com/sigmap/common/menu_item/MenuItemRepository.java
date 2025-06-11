package com.sigmap.common.menu_item;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer>,
JpaSpecificationExecutor<MenuItem>, SoftDeleteRepository<MenuItem, Integer> {
}
