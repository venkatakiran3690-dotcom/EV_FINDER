package com.smartev.repository;

import com.smartev.entity.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByStationId(Long stationId);
    List<Charger> findByStationIdAndOperationalStatus(Long stationId, String operationalStatus);
    List<Charger> findByStationIdAndAvailabilityStatus(Long stationId, String availabilityStatus);
    long countByStationCompanyId(Long companyId);
    long countByStationCompanyIdAndAvailabilityStatus(Long companyId, String availabilityStatus);
    long countByStationCompanyIdAndOperationalStatus(Long companyId, String operationalStatus);
    long countByStationId(Long stationId);
    long countByStationIdAndAvailabilityStatus(Long stationId, String availabilityStatus);
    long countByStationIdAndOperationalStatus(Long stationId, String operationalStatus);
}
