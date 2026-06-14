package com.home.tools.repository;

import com.home.tools.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByInventoryId(Long inventoryId);

    @Query("SELECT ii FROM InventoryItem ii JOIN Inventory i ON ii.inventoryId = i.id WHERE ii.toolId = :toolId ORDER BY i.inventoryDate DESC")
    List<InventoryItem> findByToolIdWithInventory(@Param("toolId") Long toolId);
}
