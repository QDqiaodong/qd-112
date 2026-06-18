package com.home.tools.service;

import com.home.tools.dto.ToolKitDTO;
import com.home.tools.dto.ToolKitItemDTO;
import com.home.tools.dto.ToolKitWithItemsDTO;
import com.home.tools.entity.ToolKit;
import java.util.List;

public interface ToolKitService {

    List<ToolKit> list();

    ToolKit getById(Long id);

    ToolKitWithItemsDTO getWithItemsById(Long id);

    ToolKit create(ToolKitDTO dto);

    ToolKit update(Long id, ToolKitDTO dto);

    void delete(Long id);

    void addItem(Long kitId, ToolKitItemDTO dto);

    void removeItem(Long kitId, Long toolId);

    void updateItem(Long kitId, ToolKitItemDTO dto);
}
