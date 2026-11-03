package com.smartev.repository;

import com.smartev.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop50ByOrderByTimestampDesc();
    List<ActivityLog> findByUserIdOrderByTimestampDesc(Long userId);
    long count();
}
