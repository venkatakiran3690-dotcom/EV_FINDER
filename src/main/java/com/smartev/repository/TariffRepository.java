package com.smartev.repository;

import com.smartev.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    List<Tariff> findByCompanyId(Long companyId);

    List<Tariff> findByCompanyIdAndStatus(Long companyId, String status);

    @Query("SELECT t FROM Tariff t WHERE t.company.id = :companyId " +
           "AND (t.station.id = :stationId OR t.station IS NULL) " +
           "AND (t.chargerType = :chargerType OR t.chargerType = 'ALL') " +
           "AND t.status = 'ACTIVE' " +
           "ORDER BY t.station.id DESC, t.effectiveFrom DESC")
    List<Tariff> findApplicableTariffs(@Param("companyId") Long companyId,
                                       @Param("stationId") Long stationId,
                                       @Param("chargerType") String chargerType);
}
