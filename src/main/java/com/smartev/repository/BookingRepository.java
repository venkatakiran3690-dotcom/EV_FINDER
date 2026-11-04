package com.smartev.repository;

import com.smartev.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Booking> findByStationIdOrderByStartTimeDesc(Long stationId);

    List<Booking> findByStationCompanyIdOrderByStartTimeDesc(Long companyId);

    @Query("SELECT b FROM Booking b WHERE b.charger.id = :chargerId " +
           "AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'IN_PROGRESS') " +
           "AND b.startTime < :requestedEnd " +
           "AND b.endTime > :requestedStart")
    List<Booking> findConflictingBookings(@Param("chargerId") Long chargerId,
                                         @Param("requestedStart") LocalDateTime requestedStart,
                                         @Param("requestedEnd") LocalDateTime requestedEnd);

    @Query("SELECT b FROM Booking b WHERE b.station.company.id = :companyId " +
           "AND b.startTime >= :dayStart AND b.startTime < :dayEnd")
    List<Booking> findCompanyBookingsForDay(@Param("companyId") Long companyId,
                                           @Param("dayStart") LocalDateTime dayStart,
                                           @Param("dayEnd") LocalDateTime dayEnd);

    @Query("SELECT b FROM Booking b WHERE b.station.id = :stationId " +
           "AND b.startTime >= :dayStart AND b.startTime < :dayEnd")
    List<Booking> findStationBookingsForDay(@Param("stationId") Long stationId,
                                           @Param("dayStart") LocalDateTime dayStart,
                                           @Param("dayEnd") LocalDateTime dayEnd);

    long countByStationCompanyId(Long companyId);
    long countByStationId(Long stationId);
    long countByStatus(String status);
}
