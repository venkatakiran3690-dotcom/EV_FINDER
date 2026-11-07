package com.smartev.repository;

import com.smartev.entity.Charger;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    long countByStationIdIn(List<Long> stationIds);
    long countByStationIdInAndAvailabilityStatus(List<Long> stationIds, String availabilityStatus);
    long countByStationIdInAndOperationalStatus(List<Long> stationIds, String operationalStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Charger c WHERE c.id = :id")
    Optional<Charger> findByIdWithPessimisticLock(@Param("id") Long id);
}
