package com.smartev.repository;

import com.smartev.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<MaintenanceRecord> findByStationIdOrderByCreatedAtDesc(Long stationId);
    List<MaintenanceRecord> findByStatus(String status);
    long countByCompanyIdAndStatus(Long companyId, String status);
    long countByStationIdAndStatus(Long stationId, String status);
}
