package com.home.tools.service;

import com.home.tools.dto.MaintenanceRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.MaintenanceRecord;
import java.util.List;

public interface MaintenanceRecordService {

    PageResult<MaintenanceRecord> list(Integer page, Integer size, Long toolId);

    List<MaintenanceRecord> findByToolId(Long toolId);

    List<MaintenanceRecord> findDueMaintenance();

    MaintenanceRecord create(MaintenanceRecordDTO dto);

    MaintenanceRecord update(Long id, MaintenanceRecordDTO dto);

    void delete(Long id);
}
