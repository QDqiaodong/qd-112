package com.home.tools.service.impl;

import com.home.tools.dto.LoanRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.LoanRecord;
import com.home.tools.entity.LoanStatus;
import com.home.tools.entity.ReturnStatus;
import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolStatus;
import com.home.tools.repository.LoanRecordRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.LoanRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanRecordServiceImpl implements LoanRecordService {

    private final LoanRecordRepository loanRecordRepository;
    private final ToolRepository toolRepository;

    public LoanRecordServiceImpl(LoanRecordRepository loanRecordRepository, ToolRepository toolRepository) {
        this.loanRecordRepository = loanRecordRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public PageResult<LoanRecord> list(Integer page, Integer size, Long toolId, LoanStatus status, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LoanRecord> result;

        if (toolId != null && status != null && startDate != null && endDate != null) {
            result = loanRecordRepository.findByToolIdAndLoanDateBetween(toolId, startDate, endDate, pageable);
        } else if (toolId != null && status != null) {
            result = loanRecordRepository.findByToolIdAndStatus(toolId, status, pageable);
        } else if (toolId != null && startDate != null && endDate != null) {
            result = loanRecordRepository.findByToolIdAndLoanDateBetween(toolId, startDate, endDate, pageable);
        } else if (toolId != null) {
            result = loanRecordRepository.findByToolId(toolId, pageable);
        } else if (status != null) {
            result = loanRecordRepository.findByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            result = loanRecordRepository.findByLoanDateBetween(startDate, endDate, pageable);
        } else {
            result = loanRecordRepository.findAll(pageable);
        }

        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public LoanRecord getById(Long id) {
        return loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借出记录不存在"));
    }

    @Override
    public List<LoanRecord> findByToolId(Long toolId) {
        return loanRecordRepository.findByToolId(toolId);
    }

    @Override
    @Transactional
    public LoanRecord create(LoanRecordDTO dto) {
        Tool tool = toolRepository.findById(dto.getToolId())
                .orElseThrow(() -> new RuntimeException("工具不存在，无法创建借出记录"));

        if (tool.getStatus() == ToolStatus.LOANED) {
            throw new RuntimeException("该工具已借出，无法重复借出");
        }

        if (tool.getStatus() == ToolStatus.MAINTENANCE) {
            throw new RuntimeException("该工具正在保养中，无法借出");
        }

        if (tool.getStatus() == ToolStatus.LOST) {
            throw new RuntimeException("该工具已遗失，无法借出");
        }

        LoanRecord record = new LoanRecord();
        copyDtoToEntity(dto, record);
        record.setStatus(LoanStatus.BORROWED);
        if (record.getLoanDate() == null) {
            record.setLoanDate(LocalDate.now());
        }

        LoanRecord savedRecord = loanRecordRepository.save(record);

        tool.setStatus(ToolStatus.LOANED);
        toolRepository.save(tool);

        return savedRecord;
    }

    @Override
    @Transactional
    public LoanRecord update(Long id, LoanRecordDTO dto) {
        LoanRecord record = loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借出记录不存在"));

        if (!record.getToolId().equals(dto.getToolId())) {
            Tool tool = toolRepository.findById(dto.getToolId())
                    .orElseThrow(() -> new RuntimeException("工具不存在"));
            if (tool.getStatus() == ToolStatus.LOANED) {
                throw new RuntimeException("该工具已借出，无法更换");
            }
        }

        copyDtoToEntity(dto, record);
        return loanRecordRepository.save(record);
    }

    @Override
    @Transactional
    public LoanRecord returnTool(Long id, LoanRecordDTO dto) {
        LoanRecord record = loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借出记录不存在"));

        if (record.getStatus() == LoanStatus.RETURNED) {
            throw new RuntimeException("该借出记录已归还，无需重复操作");
        }

        Tool tool = toolRepository.findById(record.getToolId())
                .orElseThrow(() -> new RuntimeException("工具不存在"));

        record.setStatus(LoanStatus.RETURNED);
        record.setActualReturnDate(dto.getActualReturnDate() != null ? dto.getActualReturnDate() : LocalDate.now());

        if (dto.getReturnStatus() != null) {
            record.setReturnStatus(ReturnStatus.valueOf(dto.getReturnStatus()));
        } else {
            record.setReturnStatus(ReturnStatus.GOOD);
        }

        if (dto.getRemarks() != null) {
            record.setRemarks(dto.getRemarks());
        }

        LoanRecord savedRecord = loanRecordRepository.save(record);

        ReturnStatus returnStatus = record.getReturnStatus();
        if (returnStatus == ReturnStatus.GOOD || returnStatus == ReturnStatus.DAMAGED) {
            tool.setStatus(ToolStatus.AVAILABLE);
        } else if (returnStatus == ReturnStatus.LOST) {
            tool.setStatus(ToolStatus.LOST);
        }
        toolRepository.save(tool);

        return savedRecord;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LoanRecord record = loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借出记录不存在"));

        if (record.getStatus() == LoanStatus.BORROWED) {
            Tool tool = toolRepository.findById(record.getToolId())
                    .orElseThrow(() -> new RuntimeException("工具不存在"));
            tool.setStatus(ToolStatus.AVAILABLE);
            toolRepository.save(tool);
        }

        loanRecordRepository.deleteById(id);
    }

    private void copyDtoToEntity(LoanRecordDTO dto, LoanRecord record) {
        if (dto.getToolId() != null) {
            record.setToolId(dto.getToolId());
        }
        if (dto.getBorrower() != null) {
            record.setBorrower(dto.getBorrower());
        }
        if (dto.getLoanDate() != null) {
            record.setLoanDate(dto.getLoanDate());
        }
        if (dto.getExpectedReturnDate() != null) {
            record.setExpectedReturnDate(dto.getExpectedReturnDate());
        }
        if (dto.getActualReturnDate() != null) {
            record.setActualReturnDate(dto.getActualReturnDate());
        }
        if (dto.getStatus() != null) {
            record.setStatus(LoanStatus.valueOf(dto.getStatus()));
        }
        if (dto.getReturnStatus() != null) {
            record.setReturnStatus(ReturnStatus.valueOf(dto.getReturnStatus()));
        }
        if (dto.getOperator() != null) {
            record.setOperator(dto.getOperator());
        }
        if (dto.getRemarks() != null) {
            record.setRemarks(dto.getRemarks());
        }
    }
}
