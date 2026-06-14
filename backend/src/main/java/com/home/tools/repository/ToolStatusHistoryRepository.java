package com.home.tools.repository;

import com.home.tools.entity.ToolStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ToolStatusHistoryRepository extends JpaRepository<ToolStatusHistory, Long> {

    List<ToolStatusHistory> findByToolIdOrderByCreateTimeDesc(Long toolId);
}
