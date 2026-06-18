package com.home.tools.service;

import com.home.tools.dto.CostSummaryDTO;
import java.util.List;

public interface CostSummaryService {

    List<CostSummaryDTO> getSummaryByCategory();

    List<CostSummaryDTO> getSummaryByLocation();

    List<CostSummaryDTO> getSummaryByMonth();
}
