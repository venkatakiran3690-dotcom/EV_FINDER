package com.smartev.controller;

import com.smartev.dto.TariffRequest;
import com.smartev.dto.TariffResponse;
import com.smartev.entity.Company;
import com.smartev.entity.Station;
import com.smartev.entity.Tariff;
import com.smartev.entity.User;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.StationRepository;
import com.smartev.repository.TariffRepository;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tariffs")
public class TariffController {

    private final TariffRepository tariffRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    public TariffController(TariffRepository tariffRepository,
                            StationRepository stationRepository,
                            UserRepository userRepository) {
        this.tariffRepository = tariffRepository;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<TariffResponse>> getCompanyTariffs(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getCompany() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<TariffResponse> tariffs = tariffRepository.findByCompanyId(user.getCompany().getId()).stream()
                .map(this::mapTariffToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(tariffs);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<TariffResponse> createTariff(
            @Valid @RequestBody TariffRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = user.getCompany();
        if (company == null) {
            throw new UnauthorizedException("User does not have an assigned company.");
        }

        Station station = null;
        if (request.getStationId() != null) {
            station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + request.getStationId()));
        }

        Tariff tariff = new Tariff(
                company,
                station,
                request.getChargerType().toUpperCase(),
                request.getPricePerKwh(),
                request.getServiceCharge(),
                request.getTaxPercentage()
        );

        Tariff saved = tariffRepository.save(tariff);
        return ResponseEntity.ok(mapTariffToResponse(saved));
    }

    private TariffResponse mapTariffToResponse(Tariff t) {
        TariffResponse res = new TariffResponse();
        res.setId(t.getId());
        res.setCompanyId(t.getCompany().getId());
        res.setCompanyName(t.getCompany().getName());
        if (t.getStation() != null) {
            res.setStationId(t.getStation().getId());
            res.setStationName(t.getStation().getName());
        }
        res.setChargerType(t.getChargerType());
        res.setPricePerKwh(t.getPricePerKwh());
        res.setServiceCharge(t.getServiceCharge());
        res.setTaxPercentage(t.getTaxPercentage());
        res.setStatus(t.getStatus());
        res.setEffectiveFrom(t.getEffectiveFrom());
        return res;
    }
}
