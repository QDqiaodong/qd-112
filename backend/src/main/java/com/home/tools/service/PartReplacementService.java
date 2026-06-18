package com.home.tools.service;

import com.home.tools.dto.PageResult;
import com.home.tools.dto.PartReplacementDTO;
import com.home.tools.entity.PartReplacement;

import java.time.LocalDate;
import java.util.List;

public interface PartReplacementService {

    PageResult<PartReplacement> list(Integer page, Integer size, Long toolId, String partType, LocalDate startDate, LocalDate endDate);

    List<PartReplacement> findByToolId(Long toolId);

    PartReplacement create(PartReplacementDTO dto);

    PartReplacement update(Long id, PartReplacementDTO dto);

    void delete(Long id);
}
