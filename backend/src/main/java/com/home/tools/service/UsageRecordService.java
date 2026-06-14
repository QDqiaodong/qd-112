package com.home.tools.service;

import com.home.tools.dto.PageResult;
import com.home.tools.dto.UsageRecordDTO;
import com.home.tools.entity.UsageRecord;
import java.util.List;

public interface UsageRecordService {

    PageResult<UsageRecord> list(Integer page, Integer size, Long toolId);

    List<UsageRecord> findByToolId(Long toolId);

    UsageRecord create(UsageRecordDTO dto);

    UsageRecord update(Long id, UsageRecordDTO dto);

    void delete(Long id);
}
