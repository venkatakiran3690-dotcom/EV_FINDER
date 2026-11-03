package com.smartev.repository;

import com.smartev.entity.StationManagerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationManagerAssignmentRepository extends JpaRepository<StationManagerAssignment, Long> {
    List<StationManagerAssignment> findByUserId(Long userId);
    List<StationManagerAssignment> findByStationId(Long stationId);
    boolean existsByUserIdAndStationId(Long userId, Long stationId);
    Optional<StationManagerAssignment> findByUserIdAndStationId(Long userId, Long stationId);
    void deleteByUserIdAndStationId(Long userId, Long stationId);
}
