package com.home.tools.service.impl;

import com.home.tools.dto.PageResult;
import com.home.tools.dto.ScenarioAnalysisDTO;
import com.home.tools.dto.ScenarioToolDTO;
import com.home.tools.dto.UsageRecordDTO;
import com.home.tools.entity.UsageRecord;
import com.home.tools.repository.ToolRepository;
import com.home.tools.repository.UsageRecordRepository;
import com.home.tools.service.UsageRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsageRecordServiceImpl implements UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;
    private final ToolRepository toolRepository;

    public UsageRecordServiceImpl(UsageRecordRepository usageRecordRepository, ToolRepository toolRepository) {
        this.usageRecordRepository = usageRecordRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public PageResult<UsageRecord> list(Integer page, Integer size, Long toolId, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UsageRecord> result;
        if (toolId != null && startDate != null && endDate != null) {
            result = usageRecordRepository.findByToolIdAndUseDateBetween(toolId, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            result = usageRecordRepository.findByUseDateBetween(startDate, endDate, pageable);
        } else if (toolId != null) {
            result = usageRecordRepository.findByToolId(toolId, pageable);
        } else {
            result = usageRecordRepository.findAll(pageable);
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public List<UsageRecord> findByToolId(Long toolId) {
        return usageRecordRepository.findByToolId(toolId);
    }

    @Override
    public UsageRecord create(UsageRecordDTO dto) {
        if (!toolRepository.existsById(dto.getToolId())) {
            throw new RuntimeException("工具不存在，无法创建使用记录");
        }
        UsageRecord record = new UsageRecord();
        copyDtoToEntity(dto, record);
        return usageRecordRepository.save(record);
    }

    @Override
    public UsageRecord update(Long id, UsageRecordDTO dto) {
        UsageRecord record = usageRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UsageRecord not found"));
        if (!toolRepository.existsById(dto.getToolId())) {
            throw new RuntimeException("工具不存在，无法更新使用记录");
        }
        copyDtoToEntity(dto, record);
        return usageRecordRepository.save(record);
    }

    @Override
    public void delete(Long id) {
        usageRecordRepository.deleteById(id);
    }

    @Override
    public List<ScenarioAnalysisDTO> getScenarioAnalysis(LocalDate startDate, LocalDate endDate) {
        List<Object[]> scenarioStats = usageRecordRepository.findScenarioStats(startDate, endDate);
        List<ScenarioAnalysisDTO> result = new ArrayList<>();

        for (Object[] stat : scenarioStats) {
            ScenarioAnalysisDTO dto = new ScenarioAnalysisDTO();
            dto.setScenario((String) stat[0]);
            dto.setTotalMinutes(((Number) stat[1]).intValue());
            dto.setUsageCount(((Number) stat[2]).intValue());
            dto.setLastUseDate(stat[3] != null ? stat[3].toString() : null);

            List<Object[]> toolStats = usageRecordRepository.findToolStatsByScenario(
                    dto.getScenario(), startDate, endDate);
            List<ScenarioToolDTO> topTools = new ArrayList<>();
            for (Object[] toolStat : toolStats) {
                ScenarioToolDTO toolDTO = new ScenarioToolDTO();
                toolDTO.setToolId(((Number) toolStat[0]).longValue());
                toolDTO.setToolName((String) toolStat[1]);
                toolDTO.setTotalMinutes(((Number) toolStat[2]).intValue());
                toolDTO.setUsageCount(((Number) toolStat[3]).intValue());
                toolDTO.setLastUseDate(toolStat[4] != null ? toolStat[4].toString() : null);
                topTools.add(toolDTO);
            }
            dto.setTopTools(topTools);
            result.add(dto);
        }

        return result;
    }

    private void copyDtoToEntity(UsageRecordDTO dto, UsageRecord record) {
        record.setToolId(dto.getToolId());
        record.setUseDate(dto.getUseDate());
        record.setDurationMinutes(dto.getDurationMinutes());
        record.setScenario(dto.getScenario());
        record.setOperator(dto.getOperator());
        record.setRemarks(dto.getRemarks());
    }
}
