package com.home.tools.repository;

import com.home.tools.entity.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    Page<UsageRecord> findByToolId(Long toolId, Pageable pageable);

    List<UsageRecord> findByToolId(Long toolId);

    List<UsageRecord> findByToolIdOrderByUseDateDesc(Long toolId);

    Page<UsageRecord> findByUseDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<UsageRecord> findByToolIdAndUseDateBetween(Long toolId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    void deleteByToolId(Long toolId);

    @Query("SELECT DISTINCT u.scenario FROM UsageRecord u WHERE u.scenario IS NOT NULL AND u.scenario <> ''")
    List<String> findAllScenarios();

    @Query("SELECT u.scenario, SUM(u.durationMinutes), COUNT(u), MAX(u.useDate) " +
           "FROM UsageRecord u WHERE u.scenario IS NOT NULL AND u.scenario <> '' " +
           "AND (:startDate IS NULL OR u.useDate >= :startDate) " +
           "AND (:endDate IS NULL OR u.useDate <= :endDate) " +
           "GROUP BY u.scenario ORDER BY SUM(u.durationMinutes) DESC")
    List<Object[]> findScenarioStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT u.toolId, t.name, SUM(u.durationMinutes), COUNT(u), MAX(u.useDate) " +
           "FROM UsageRecord u JOIN Tool t ON u.toolId = t.id " +
           "WHERE u.scenario = :scenario " +
           "AND (:startDate IS NULL OR u.useDate >= :startDate) " +
           "AND (:endDate IS NULL OR u.useDate <= :endDate) " +
           "GROUP BY u.toolId, t.name ORDER BY SUM(u.durationMinutes) DESC")
    List<Object[]> findToolStatsByScenario(@Param("scenario") String scenario,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    Long countByToolIdAndUseDateBetween(Long toolId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT MAX(u.useDate) FROM UsageRecord u WHERE u.toolId = :toolId")
    LocalDate findLastUseDateByToolId(@Param("toolId") Long toolId);
}
