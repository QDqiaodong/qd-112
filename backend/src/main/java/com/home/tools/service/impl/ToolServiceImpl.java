package com.home.tools.service.impl;

import com.home.tools.dto.PageResult;
import com.home.tools.dto.ToolDTO;
import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolStatus;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.ToolService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolServiceImpl implements ToolService {

    private final ToolRepository toolRepository;

    public ToolServiceImpl(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    @Override
    public PageResult<Tool> list(Integer page, Integer size, String keyword, Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Tool> result;
        if (categoryId != null) {
            result = toolRepository.findByCategoryId(categoryId, pageable);
        } else if (StringUtils.hasText(keyword)) {
            result = toolRepository.findByNameContainingOrModelContainingOrBrandContaining(keyword, keyword, keyword, pageable);
        } else {
            result = toolRepository.findAll(pageable);
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public Tool getById(Long id) {
        return toolRepository.findById(id).orElseThrow(() -> new RuntimeException("Tool not found"));
    }

    @Override
    public Tool create(ToolDTO dto) {
        Tool tool = new Tool();
        copyDtoToEntity(dto, tool);
        if (dto.getStatus() != null) {
            tool.setStatus(ToolStatus.valueOf(dto.getStatus()));
        } else {
            tool.setStatus(ToolStatus.AVAILABLE);
        }
        if (tool.getNextMaintenanceDate() == null && tool.getPurchaseDate() != null && tool.getMaintenanceCycleDays() != null) {
            tool.setNextMaintenanceDate(tool.getPurchaseDate().plusDays(tool.getMaintenanceCycleDays()));
        }
        return toolRepository.save(tool);
    }

    @Override
    public Tool update(Long id, ToolDTO dto) {
        Tool tool = getById(id);
        copyDtoToEntity(dto, tool);
        if (dto.getStatus() != null) {
            tool.setStatus(ToolStatus.valueOf(dto.getStatus()));
        }
        return toolRepository.save(tool);
    }

    @Override
    public void delete(Long id) {
        toolRepository.deleteById(id);
    }

    @Override
    public List<Tool> findByCategory(Long categoryId) {
        return toolRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Tool> findDueMaintenance() {
        return toolRepository.findByNextMaintenanceDateBefore(LocalDate.now());
    }

    @Override
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ToolStatus status : ToolStatus.values()) {
            counts.put(status.name(), (long) toolRepository.findByStatus(status).size());
        }
        return counts;
    }

    private void copyDtoToEntity(ToolDTO dto, Tool tool) {
        tool.setName(dto.getName());
        tool.setModel(dto.getModel());
        tool.setBrand(dto.getBrand());
        tool.setCategoryId(dto.getCategoryId());
        tool.setSubCategoryId(dto.getSubCategoryId());
        tool.setPurpose(dto.getPurpose());
        tool.setSpecification(dto.getSpecification());
        tool.setLocation(dto.getLocation());
        tool.setPurchaseDate(dto.getPurchaseDate());
        tool.setPrice(dto.getPrice());
        tool.setLastMaintenanceDate(dto.getLastMaintenanceDate());
        tool.setNextMaintenanceDate(dto.getNextMaintenanceDate());
        tool.setMaintenanceCycleDays(dto.getMaintenanceCycleDays());
    }
}
