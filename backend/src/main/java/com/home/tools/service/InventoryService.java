package com.home.tools.service;

import com.home.tools.dto.InventoryDTO;
import com.home.tools.dto.InventoryItemDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.Inventory;
import com.home.tools.entity.InventoryItem;
import java.util.List;

public interface InventoryService {

    PageResult<Inventory> listInventories(Integer page, Integer size);

    Inventory getInventoryDetail(Long id);

    Inventory createInventory(InventoryDTO dto);

    Inventory updateInventory(Long id, InventoryDTO dto);

    InventoryItem updateItem(Long inventoryId, InventoryItemDTO dto);
}
