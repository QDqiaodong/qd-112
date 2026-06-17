package com.home.tools.service;

import com.home.tools.dto.MaintenanceTrackDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.StatusTransitionResult;
import com.home.tools.dto.ToolAvailabilityScore;
import com.home.tools.dto.ToolDTO;
import com.home.tools.dto.ToolWithScore;
import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolStatus;
import java.util.List;
import java.util.Map;

public interface ToolService {

    PageResult<Tool> list(Integer page, Integer size, String keyword, Long categoryId);

    PageResult<ToolWithScore> listWithScore(Integer page, Integer size, String keyword, Long categoryId);

    Tool getById(Long id);

    Tool create(ToolDTO dto);

    Tool update(Long id, ToolDTO dto);

    void delete(Long id);

    List<Tool> findByCategory(Long categoryId);

    List<Tool> findDueMaintenance();

    List<Tool> findMaintenanceByMonth(int year, int month);

    Map<String, Long> countByStatus();

    Map<String, Long> countByLocation();

    List<MaintenanceTrackDTO> getMaintenanceTrack(Long toolId);

    ToolAvailabilityScore calculateAvailabilityScore(Long toolId);

    ToolAvailabilityScore calculateAvailabilityScore(Tool tool);

    StatusTransitionResult validateStatusTransition(Long toolId, ToolStatus newStatus);

    List<ToolStatus> getAllowedStatusTransitions(ToolStatus currentStatus);
}
