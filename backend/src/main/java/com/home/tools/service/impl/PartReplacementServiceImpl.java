package com.home.tools.service.impl;

import com.home.tools.dto.PageResult;
import com.home.tools.dto.PartReplacementDTO;
import com.home.tools.entity.PartReplacement;
import com.home.tools.repository.PartReplacementRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.PartReplacementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PartReplacementServiceImpl implements PartReplacementService {

    private final PartReplacementRepository partReplacementRepository;
    private final ToolRepository toolRepository;

    public PartReplacementServiceImpl(PartReplacementRepository partReplacementRepository, ToolRepository toolRepository) {
        this.partReplacementRepository = partReplacementRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public PageResult<PartReplacement> list(Integer page, Integer size, Long toolId, String partType, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<PartReplacement> result;

        if (toolId != null && startDate != null && endDate != null) {
            result = partReplacementRepository.findByToolIdAndReplacementDateBetween(toolId, startDate, endDate, pageable);
        } else if (toolId != null && partType != null) {
            result = partReplacementRepository.findByToolIdAndPartType(toolId, partType, pageable);
        } else if (startDate != null && endDate != null) {
            result = partReplacementRepository.findByReplacementDateBetween(startDate, endDate, pageable);
        } else if (toolId != null) {
            result = partReplacementRepository.findByToolId(toolId, pageable);
        } else if (partType != null) {
            result = partReplacementRepository.findByPartType(partType, pageable);
        } else {
            result = partReplacementRepository.findAll(pageable);
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public List<PartReplacement> findByToolId(Long toolId) {
        return partReplacementRepository.findByToolIdOrderByReplacementDateDesc(toolId);
    }

    @Override
    public PartReplacement create(PartReplacementDTO dto) {
        if (!toolRepository.existsById(dto.getToolId())) {
            throw new RuntimeException("工具不存在，无法创建零件更换记录");
        }
        PartReplacement record = new PartReplacement();
        copyDtoToEntity(dto, record);
        return partReplacementRepository.save(record);
    }

    @Override
    public PartReplacement update(Long id, PartReplacementDTO dto) {
        PartReplacement record = partReplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PartReplacement not found"));
        if (!toolRepository.existsById(dto.getToolId())) {
            throw new RuntimeException("工具不存在，无法更新零件更换记录");
        }
        copyDtoToEntity(dto, record);
        return partReplacementRepository.save(record);
    }

    @Override
    public void delete(Long id) {
        partReplacementRepository.deleteById(id);
    }

    private void copyDtoToEntity(PartReplacementDTO dto, PartReplacement record) {
        record.setToolId(dto.getToolId());
        record.setPartName(dto.getPartName());
        record.setPartType(dto.getPartType());
        record.setReplacementDate(dto.getReplacementDate());
        record.setCost(dto.getCost());
        record.setOperator(dto.getOperator());
        record.setSupplier(dto.getSupplier());
        record.setRemarks(dto.getRemarks());
    }
}
