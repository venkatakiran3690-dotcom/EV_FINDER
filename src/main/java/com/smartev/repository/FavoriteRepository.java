package com.smartev.repository;

import com.smartev.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Favorite> findByStationId(Long stationId);
    boolean existsByDriverIdAndStationId(Long driverId, Long stationId);
    Optional<Favorite> findByDriverIdAndStationId(Long driverId, Long stationId);
    void deleteByDriverIdAndStationId(Long driverId, Long stationId);
}
