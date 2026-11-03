package com.smartev.repository;

import com.smartev.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStationIdOrderByCreatedAtDesc(Long stationId);

    List<Review> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.station.id = :stationId")
    Double getAverageRatingForStation(@Param("stationId") Long stationId);

    @Query("SELECT r FROM Review r WHERE r.station.company.id = :companyId ORDER BY r.createdAt DESC")
    List<Review> findByCompanyId(@Param("companyId") Long companyId);
}
