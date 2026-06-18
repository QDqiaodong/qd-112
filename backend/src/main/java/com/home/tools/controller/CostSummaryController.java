package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.CostSummaryDTO;
import com.home.tools.service.CostSummaryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cost-summary")
public class CostSummaryController {

    private final CostSummaryService costSummaryService;

    public CostSummaryController(CostSummaryService costSummaryService) {
        this.costSummaryService = costSummaryService;
    }

    @GetMapping("/by-category")
    public ApiResponse<List<CostSummaryDTO>> getSummaryByCategory() {
        return ApiResponse.ok(costSummaryService.getSummaryByCategory());
    }

    @GetMapping("/by-location")
    public ApiResponse<List<CostSummaryDTO>> getSummaryByLocation() {
        return ApiResponse.ok(costSummaryService.getSummaryByLocation());
    }

    @GetMapping("/by-month")
    public ApiResponse<List<CostSummaryDTO>> getSummaryByMonth() {
        return ApiResponse.ok(costSummaryService.getSummaryByMonth());
    }
}
