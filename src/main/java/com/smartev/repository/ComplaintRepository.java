package com.smartev.repository;

import com.smartev.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Complaint> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<Complaint> findByStationIdOrderByCreatedAtDesc(Long stationId);
    List<Complaint> findByStatus(String status);
    long countByCompanyId(Long companyId);
    long countByCompanyIdAndStatus(Long companyId, String status);
}
