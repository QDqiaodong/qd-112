package com.home.tools.service;

import com.home.tools.dto.MaintenanceTrackDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.ToolDTO;
import com.home.tools.entity.Tool;
import java.util.List;
import java.util.Map;

public interface ToolService {

    PageResult<Tool> list(Integer page, Integer size, String keyword, Long categoryId);

    Tool getById(Long id);

    Tool create(ToolDTO dto);

    Tool update(Long id, ToolDTO dto);

    void delete(Long id);

    List<Tool> findByCategory(Long categoryId);

    List<Tool> findDueMaintenance();

    List<Tool> findMaintenanceByMonth(int year, int month);

    Map<String, Long> countByStatus();

    List<MaintenanceTrackDTO> getMaintenanceTrack(Long toolId);
}
