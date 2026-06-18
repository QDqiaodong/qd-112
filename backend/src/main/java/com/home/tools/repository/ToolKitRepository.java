package com.home.tools.repository;

import com.home.tools.entity.ToolKit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ToolKitRepository extends JpaRepository<ToolKit, Long> {

    List<ToolKit> findAllByOrderBySortOrderAscCreateTimeDesc();
}
