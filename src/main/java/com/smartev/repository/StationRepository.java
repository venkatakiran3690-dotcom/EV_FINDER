package com.smartev.repository;

import com.smartev.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByCompanyId(Long companyId);
    List<Station> findByOperationalStatus(String operationalStatus);

    @Query("SELECT s FROM Station s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.city) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.address) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Station> searchStations(@Param("query") String query);

    long countByCompanyId(Long companyId);
}
