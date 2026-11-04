package com.smartev.service;

import com.smartev.dto.VehicleRequest;
import com.smartev.dto.VehicleResponse;
import com.smartev.entity.User;
import com.smartev.entity.Vehicle;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesForDriver(User driver) {
        return vehicleRepository.findByDriverId(driver.getId()).stream()
                .map(this::mapVehicleToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long vehicleId, User driver) {
        Vehicle vehicle = vehicleRepository.findByIdAndDriverId(vehicleId, driver.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        return mapVehicleToResponse(vehicle);
    }

    @Transactional
    public VehicleResponse registerVehicle(VehicleRequest request, User driver) {
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new BadRequestException("A vehicle with license plate '" + request.getLicensePlate() + "' is already registered.");
        }

        Vehicle vehicle = new Vehicle(
                driver,
                request.getMake(),
                request.getModel(),
                request.getYear(),
                request.getLicensePlate(),
                request.getBatteryCapacityKwh(),
                request.getCompatibleConnector()
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapVehicleToResponse(saved);
    }

    @Transactional
    public VehicleResponse updateVehicle(Long vehicleId, VehicleRequest request, User driver) {
        Vehicle vehicle = vehicleRepository.findByIdAndDriverId(vehicleId, driver.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setBatteryCapacityKwh(request.getBatteryCapacityKwh());
        vehicle.setCompatibleConnector(request.getCompatibleConnector());

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapVehicleToResponse(updated);
    }

    @Transactional
    public void deleteVehicle(Long vehicleId, User driver) {
        Vehicle vehicle = vehicleRepository.findByIdAndDriverId(vehicleId, driver.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse mapVehicleToResponse(Vehicle v) {
        return new VehicleResponse(
                v.getId(),
                v.getDriver().getId(),
                v.getMake(),
                v.getModel(),
                v.getYear(),
                v.getLicensePlate(),
                v.getBatteryCapacityKwh(),
                v.getCompatibleConnector(),
                v.getCreatedAt()
        );
    }
}
