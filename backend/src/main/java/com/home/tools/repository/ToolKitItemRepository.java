package com.home.tools.repository;

import com.home.tools.entity.ToolKitItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ToolKitItemRepository extends JpaRepository<ToolKitItem, Long> {

    List<ToolKitItem> findByKitId(Long kitId);

    void deleteByKitId(Long kitId);

    void deleteByKitIdAndToolId(Long kitId, Long toolId);
}
