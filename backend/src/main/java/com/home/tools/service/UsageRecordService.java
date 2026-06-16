package com.home.tools.service;

import com.home.tools.dto.PageResult;
import com.home.tools.dto.ScenarioAnalysisDTO;
import com.home.tools.dto.UsageRecordDTO;
import com.home.tools.entity.UsageRecord;

import java.time.LocalDate;
import java.util.List;

public interface UsageRecordService {

    PageResult<UsageRecord> list(Integer page, Integer size, Long toolId, LocalDate startDate, LocalDate endDate);

    List<UsageRecord> findByToolId(Long toolId);

    UsageRecord create(UsageRecordDTO dto);

    UsageRecord update(Long id, UsageRecordDTO dto);

    void delete(Long id);

    List<ScenarioAnalysisDTO> getScenarioAnalysis(LocalDate startDate, LocalDate endDate);
}
