package com.home.tools.service;

import com.home.tools.dto.LoanRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.LoanRecord;
import com.home.tools.entity.LoanStatus;

import java.time.LocalDate;
import java.util.List;

public interface LoanRecordService {

    PageResult<LoanRecord> list(Integer page, Integer size, Long toolId, LoanStatus status, LocalDate startDate, LocalDate endDate);

    LoanRecord getById(Long id);

    List<LoanRecord> findByToolId(Long toolId);

    LoanRecord create(LoanRecordDTO dto);

    LoanRecord update(Long id, LoanRecordDTO dto);

    LoanRecord returnTool(Long id, LoanRecordDTO dto);

    void delete(Long id);
}
