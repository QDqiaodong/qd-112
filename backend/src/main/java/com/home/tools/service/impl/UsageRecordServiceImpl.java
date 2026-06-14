package com.home.tools.service.impl;

import com.home.tools.dto.PageResult;
import com.home.tools.dto.UsageRecordDTO;
import com.home.tools.entity.UsageRecord;
import com.home.tools.repository.UsageRecordRepository;
import com.home.tools.service.UsageRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsageRecordServiceImpl implements UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;

    public UsageRecordServiceImpl(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    @Override
    public PageResult<UsageRecord> list(Integer page, Integer size, Long toolId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UsageRecord> result;
        if (toolId != null) {
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
        UsageRecord record = new UsageRecord();
        copyDtoToEntity(dto, record);
        return usageRecordRepository.save(record);
    }

    @Override
    public UsageRecord update(Long id, UsageRecordDTO dto) {
        UsageRecord record = usageRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UsageRecord not found"));
        copyDtoToEntity(dto, record);
        return usageRecordRepository.save(record);
    }

    @Override
    public void delete(Long id) {
        usageRecordRepository.deleteById(id);
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
