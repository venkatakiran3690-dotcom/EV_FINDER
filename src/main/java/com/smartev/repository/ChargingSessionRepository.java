package com.smartev.repository;

import com.smartev.entity.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    Optional<ChargingSession> findBySessionCode(String sessionCode);

    Optional<ChargingSession> findByBookingId(Long bookingId);

    List<ChargingSession> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    Optional<ChargingSession> findFirstByDriverIdAndStatus(Long driverId, String status);

    List<ChargingSession> findByChargerIdAndStatus(Long chargerId, String status);

    @Query("SELECT s FROM ChargingSession s WHERE s.charger.station.company.id = :companyId ORDER BY s.createdAt DESC")
    List<ChargingSession> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT s FROM ChargingSession s WHERE s.charger.station.id = :stationId ORDER BY s.createdAt DESC")
    List<ChargingSession> findByStationId(@Param("stationId") Long stationId);

    @Query("SELECT s FROM ChargingSession s WHERE s.charger.station.id IN :stationIds ORDER BY s.createdAt DESC")
    List<ChargingSession> findByStationIdInOrderByCreatedAtDesc(@Param("stationIds") List<Long> stationIds);

    @Query("SELECT COUNT(s) FROM ChargingSession s WHERE s.charger.station.id IN :stationIds AND s.status = :status")
    long countByStationIdInAndStatus(@Param("stationIds") List<Long> stationIds, @Param("status") String status);

    @Query("SELECT COUNT(s) FROM ChargingSession s WHERE s.charger.station.company.id = :companyId AND s.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status);

    long countByStatus(String status);
}
