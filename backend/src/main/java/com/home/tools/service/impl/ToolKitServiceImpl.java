package com.home.tools.service.impl;

import com.home.tools.dto.ToolKitDTO;
import com.home.tools.dto.ToolKitItemDTO;
import com.home.tools.dto.ToolKitItemDetailDTO;
import com.home.tools.dto.ToolKitWithItemsDTO;
import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolKit;
import com.home.tools.entity.ToolKitItem;
import com.home.tools.entity.ToolStatus;
import com.home.tools.repository.ToolKitItemRepository;
import com.home.tools.repository.ToolKitRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.ToolKitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ToolKitServiceImpl implements ToolKitService {

    private final ToolKitRepository toolKitRepository;
    private final ToolKitItemRepository toolKitItemRepository;
    private final ToolRepository toolRepository;

    public ToolKitServiceImpl(ToolKitRepository toolKitRepository,
                              ToolKitItemRepository toolKitItemRepository,
                              ToolRepository toolRepository) {
        this.toolKitRepository = toolKitRepository;
        this.toolKitItemRepository = toolKitItemRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public List<ToolKit> list() {
        return toolKitRepository.findAllByOrderBySortOrderAscCreateTimeDesc();
    }

    @Override
    public ToolKit getById(Long id) {
        return toolKitRepository.findById(id).orElseThrow(() -> new RuntimeException("ToolKit not found"));
    }

    @Override
    public ToolKitWithItemsDTO getWithItemsById(Long id) {
        ToolKit kit = getById(id);
        List<ToolKitItem> items = toolKitItemRepository.findByKitId(id);

        ToolKitWithItemsDTO result = new ToolKitWithItemsDTO();
        result.setKit(kit);

        List<ToolKitItemDetailDTO> detailItems = new ArrayList<>();
        int availableCount = 0;
        int missingCount = 0;

        for (ToolKitItem item : items) {
            ToolKitItemDetailDTO detail = new ToolKitItemDetailDTO();
            detail.setItem(item);

            Tool tool = toolRepository.findById(item.getToolId()).orElse(null);
            detail.setTool(tool);

            if (tool == null) {
                detail.setAvailable(false);
                detail.setMissingReason("工具已不存在");
                missingCount++;
            } else if (tool.getStatus() == ToolStatus.LOST) {
                detail.setAvailable(false);
                detail.setMissingReason("工具已丢失");
                missingCount++;
            } else if (tool.getStatus() == ToolStatus.MAINTENANCE) {
                detail.setAvailable(false);
                detail.setMissingReason("工具正在保养中");
                missingCount++;
            } else if (tool.getStatus() == ToolStatus.LOANED) {
                detail.setAvailable(false);
                detail.setMissingReason("工具已借出");
                missingCount++;
            } else {
                detail.setAvailable(true);
                availableCount++;
            }

            detailItems.add(detail);
        }

        result.setItems(detailItems);
        result.setTotalItems(detailItems.size());
        result.setAvailableItems(availableCount);
        result.setMissingItems(missingCount);

        return result;
    }

    @Override
    public ToolKit create(ToolKitDTO dto) {
        ToolKit kit = new ToolKit();
        kit.setName(dto.getName());
        kit.setDescription(dto.getDescription());
        kit.setScenario(dto.getScenario());
        kit.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        return toolKitRepository.save(kit);
    }

    @Override
    public ToolKit update(Long id, ToolKitDTO dto) {
        ToolKit kit = getById(id);
        kit.setName(dto.getName());
        kit.setDescription(dto.getDescription());
        kit.setScenario(dto.getScenario());
        if (dto.getSortOrder() != null) {
            kit.setSortOrder(dto.getSortOrder());
        }
        return toolKitRepository.save(kit);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        toolKitItemRepository.deleteByKitId(id);
        toolKitRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addItem(Long kitId, ToolKitItemDTO dto) {
        ToolKit kit = getById(kitId);
        if (!toolRepository.existsById(dto.getToolId())) {
            throw new RuntimeException("Tool not found");
        }
        ToolKitItem item = new ToolKitItem();
        item.setKitId(kitId);
        item.setToolId(dto.getToolId());
        item.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        item.setRemarks(dto.getRemarks());
        toolKitItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeItem(Long kitId, Long toolId) {
        toolKitItemRepository.deleteByKitIdAndToolId(kitId, toolId);
    }

    @Override
    @Transactional
    public void updateItem(Long kitId, ToolKitItemDTO dto) {
        List<ToolKitItem> items = toolKitItemRepository.findByKitId(kitId);
        ToolKitItem existing = items.stream()
                .filter(i -> i.getToolId().equals(dto.getToolId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ToolKitItem not found"));
        if (dto.getQuantity() != null) {
            existing.setQuantity(dto.getQuantity());
        }
        if (dto.getRemarks() != null) {
            existing.setRemarks(dto.getRemarks());
        }
        toolKitItemRepository.save(existing);
    }
}
