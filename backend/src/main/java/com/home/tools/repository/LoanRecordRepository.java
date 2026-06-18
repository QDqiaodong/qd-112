package com.home.tools.repository;

import com.home.tools.entity.LoanRecord;
import com.home.tools.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long> {

    Page<LoanRecord> findByToolId(Long toolId, Pageable pageable);

    List<LoanRecord> findByToolId(Long toolId);

    Page<LoanRecord> findByStatus(LoanStatus status, Pageable pageable);

    Page<LoanRecord> findByToolIdAndStatus(Long toolId, LoanStatus status, Pageable pageable);

    Page<LoanRecord> findByLoanDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<LoanRecord> findByToolIdAndLoanDateBetween(Long toolId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<LoanRecord> findByBorrowerContaining(String borrower);

    boolean existsByToolIdAndStatus(Long toolId, LoanStatus status);
}
