package com.home.tools.service.impl;

import com.home.tools.dto.MaintenanceRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.MaintenanceRecord;
import com.home.tools.entity.MaintenanceType;
import com.home.tools.entity.Tool;
import com.home.tools.repository.MaintenanceRecordRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.MaintenanceRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final ToolRepository toolRepository;

    public MaintenanceRecordServiceImpl(MaintenanceRecordRepository maintenanceRecordRepository,
                                        ToolRepository toolRepository) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public PageResult<MaintenanceRecord> list(Integer page, Integer size, Long toolId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<MaintenanceRecord> result;
        if (toolId != null) {
            result = maintenanceRecordRepository.findByToolId(toolId, pageable);
        } else {
            result = maintenanceRecordRepository.findAll(pageable);
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public List<MaintenanceRecord> findByToolId(Long toolId) {
        return maintenanceRecordRepository.findByToolId(toolId);
    }

    @Override
    public List<MaintenanceRecord> findDueMaintenance() {
        List<Tool> dueTools = toolRepository.findByNextMaintenanceDateBefore(LocalDate.now());
        List<MaintenanceRecord> records = new ArrayList<>();
        for (Tool tool : dueTools) {
            List<MaintenanceRecord> toolRecords = maintenanceRecordRepository.findByToolId(tool.getId());
            if (!toolRecords.isEmpty()) {
                records.add(toolRecords.get(0));
            }
        }
        return records;
    }

    @Override
    @Transactional
    public MaintenanceRecord create(MaintenanceRecordDTO dto) {
        Tool tool = toolRepository.findById(dto.getToolId())
                .orElseThrow(() -> new RuntimeException("工具不存在，无法创建保养记录"));
        MaintenanceRecord record = new MaintenanceRecord();
        copyDtoToEntity(dto, record);
        calculateTotalCost(record);
        if (record.getNextMaintenanceDate() == null && tool.getMaintenanceCycleDays() != null) {
            record.setNextMaintenanceDate(record.getMaintenanceDate().plusDays(tool.getMaintenanceCycleDays()));
        }
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        tool.setLastMaintenanceDate(record.getMaintenanceDate());
        if (record.getNextMaintenanceDate() != null) {
            tool.setNextMaintenanceDate(record.getNextMaintenanceDate());
        }
        toolRepository.save(tool);
        return saved;
    }

    @Override
    @Transactional
    public MaintenanceRecord update(Long id, MaintenanceRecordDTO dto) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaintenanceRecord not found"));
        Tool tool = toolRepository.findById(dto.getToolId())
                .orElseThrow(() -> new RuntimeException("工具不存在，无法更新保养记录"));
        copyDtoToEntity(dto, record);
        calculateTotalCost(record);
        if (record.getNextMaintenanceDate() == null && tool.getMaintenanceCycleDays() != null) {
            record.setNextMaintenanceDate(record.getMaintenanceDate().plusDays(tool.getMaintenanceCycleDays()));
        }
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        tool.setLastMaintenanceDate(record.getMaintenanceDate());
        if (record.getNextMaintenanceDate() != null) {
            tool.setNextMaintenanceDate(record.getNextMaintenanceDate());
        }
        toolRepository.save(tool);
        return saved;
    }

    @Override
    public void delete(Long id) {
        maintenanceRecordRepository.deleteById(id);
    }

    private void copyDtoToEntity(MaintenanceRecordDTO dto, MaintenanceRecord record) {
        record.setToolId(dto.getToolId());
        record.setMaintenanceType(MaintenanceType.valueOf(dto.getMaintenanceType()));
        record.setMaintenanceDate(dto.getMaintenanceDate());
        record.setOperator(dto.getOperator());
        record.setCost(dto.getCost());
        record.setLaborCost(dto.getLaborCost());
        record.setPartsCost(dto.getPartsCost());
        record.setOtherCost(dto.getOtherCost());
        record.setPartsReplaced(dto.getPartsReplaced());
        record.setDescription(dto.getDescription());
        record.setNextMaintenanceDate(dto.getNextMaintenanceDate());
    }

    private void calculateTotalCost(MaintenanceRecord record) {
        BigDecimal labor = record.getLaborCost() != null ? record.getLaborCost() : BigDecimal.ZERO;
        BigDecimal parts = record.getPartsCost() != null ? record.getPartsCost() : BigDecimal.ZERO;
        BigDecimal other = record.getOtherCost() != null ? record.getOtherCost() : BigDecimal.ZERO;
        BigDecimal total = labor.add(parts).add(other);
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            record.setCost(total);
        }
    }
}
