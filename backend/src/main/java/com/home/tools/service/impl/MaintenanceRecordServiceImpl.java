package com.home.tools.service.impl;

import com.home.tools.dto.MaintenanceRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.MaintenanceItem;
import com.home.tools.entity.MaintenanceRecord;
import com.home.tools.entity.MaintenanceType;
import com.home.tools.entity.Tool;
import com.home.tools.repository.MaintenanceItemRepository;
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
import java.util.Optional;

@Service
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final ToolRepository toolRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;

    public MaintenanceRecordServiceImpl(MaintenanceRecordRepository maintenanceRecordRepository,
                                        ToolRepository toolRepository,
                                        MaintenanceItemRepository maintenanceItemRepository) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.toolRepository = toolRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
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
        List<Tool> dueTools = toolRepository.findByNextMaintenanceDateBeforeOrEqual(LocalDate.now());
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
        resolveNextMaintenanceDate(record, tool);
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
        Long oldToolId = record.getToolId();
        Long newToolId = dto.getToolId();

        Tool newTool = toolRepository.findById(newToolId)
                .orElseThrow(() -> new RuntimeException("工具不存在，无法更新保养记录"));
        copyDtoToEntity(dto, record);
        calculateTotalCost(record);
        resolveNextMaintenanceDate(record, newTool);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        newTool.setLastMaintenanceDate(record.getMaintenanceDate());
        if (record.getNextMaintenanceDate() != null) {
            newTool.setNextMaintenanceDate(record.getNextMaintenanceDate());
        }
        toolRepository.save(newTool);

        if (!oldToolId.equals(newToolId)) {
            Tool oldTool = toolRepository.findById(oldToolId)
                    .orElseThrow(() -> new RuntimeException("原工具不存在"));
            List<MaintenanceRecord> oldToolRecords = maintenanceRecordRepository.findByToolIdOrderByMaintenanceDateDesc(oldToolId);
            if (!oldToolRecords.isEmpty()) {
                MaintenanceRecord latestRecord = oldToolRecords.get(0);
                oldTool.setLastMaintenanceDate(latestRecord.getMaintenanceDate());
                if (latestRecord.getNextMaintenanceDate() != null) {
                    oldTool.setNextMaintenanceDate(latestRecord.getNextMaintenanceDate());
                } else {
                    resolveNextMaintenanceDate(latestRecord, oldTool);
                    oldTool.setNextMaintenanceDate(latestRecord.getNextMaintenanceDate());
                }
            } else {
                oldTool.setLastMaintenanceDate(null);
                if (oldTool.getPurchaseDate() != null && oldTool.getMaintenanceCycleDays() != null) {
                    oldTool.setNextMaintenanceDate(oldTool.getPurchaseDate().plusDays(oldTool.getMaintenanceCycleDays()));
                } else {
                    oldTool.setNextMaintenanceDate(null);
                }
            }
            toolRepository.save(oldTool);
        }

        return saved;
    }

    private void resolveNextMaintenanceDate(MaintenanceRecord record, Tool tool) {
        if (record.getNextMaintenanceDate() != null) {
            return;
        }
        Integer cycleDays = null;
        if (record.getMaintenanceType() != null) {
            String typeCode = record.getMaintenanceType().name().toLowerCase();
            Optional<MaintenanceItem> itemOpt = maintenanceItemRepository.findByCode(typeCode);
            if (itemOpt.isPresent() && itemOpt.get().getDefaultCycleDays() != null) {
                cycleDays = itemOpt.get().getDefaultCycleDays();
            }
        }
        if (cycleDays == null && tool.getMaintenanceCycleDays() != null) {
            cycleDays = tool.getMaintenanceCycleDays();
        }
        if (cycleDays != null) {
            record.setNextMaintenanceDate(record.getMaintenanceDate().plusDays(cycleDays));
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("保养记录不存在"));
        Long toolId = record.getToolId();
        maintenanceRecordRepository.deleteById(id);
        maintenanceRecordRepository.flush();

        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new RuntimeException("工具不存在"));

        List<MaintenanceRecord> remainingRecords = maintenanceRecordRepository.findByToolIdOrderByMaintenanceDateDesc(toolId);
        if (!remainingRecords.isEmpty()) {
            MaintenanceRecord latestRecord = remainingRecords.get(0);
            tool.setLastMaintenanceDate(latestRecord.getMaintenanceDate());
            if (latestRecord.getNextMaintenanceDate() != null) {
                tool.setNextMaintenanceDate(latestRecord.getNextMaintenanceDate());
            } else {
                resolveNextMaintenanceDate(latestRecord, tool);
                tool.setNextMaintenanceDate(latestRecord.getNextMaintenanceDate());
            }
        } else {
            tool.setLastMaintenanceDate(null);
            if (tool.getPurchaseDate() != null && tool.getMaintenanceCycleDays() != null) {
                tool.setNextMaintenanceDate(tool.getPurchaseDate().plusDays(tool.getMaintenanceCycleDays()));
            } else {
                tool.setNextMaintenanceDate(null);
            }
        }
        toolRepository.save(tool);
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
