package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.VehicleCreateRequest;
import com.ridelink.drivervehicle.dto.VehicleResponse;
import com.ridelink.drivervehicle.dto.VehicleUpdateRequest;
import com.ridelink.drivervehicle.entity.Driver;
import com.ridelink.drivervehicle.entity.Vehicle;
import com.ridelink.drivervehicle.exception.DuplicateResourceException;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleResponse registerVehicle(VehicleCreateRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .filter(Driver::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active driver not found with ID: " + request.getDriverId()
                ));

        String registrationNumber =
                normalizeRegistrationNumber(request.getRegistrationNumber());

        if (vehicleRepository.existsByRegistrationNumberIgnoreCase(registrationNumber)) {
            throw new DuplicateResourceException(
                    "Vehicle already exists with registration number: "
                            + registrationNumber
            );
        }

        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .registrationNumber(registrationNumber)
                .make(request.getMake().trim())
                .model(request.getModel().trim())
                .manufacturingYear(request.getManufacturingYear())
                .color(request.getColor().trim())
                .vehicleType(request.getVehicleType())
                .seatCapacity(request.getSeatCapacity())
                .active(true)
                .build();

        return mapToResponse(vehicleRepository.saveAndFlush(vehicle));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long vehicleId) {
        return mapToResponse(findActiveVehicle(vehicleId));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByDriver(Long driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(
                    "Driver not found with ID: " + driverId
            );
        }

        return vehicleRepository.findByDriver_IdAndActiveTrue(driverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VehicleResponse updateVehicle(
            Long vehicleId,
            VehicleUpdateRequest request
    ) {
        Vehicle vehicle = findActiveVehicle(vehicleId);

        String registrationNumber =
                normalizeRegistrationNumber(request.getRegistrationNumber());

        if (vehicleRepository.existsByRegistrationNumberIgnoreCaseAndIdNot(
                registrationNumber,
                vehicleId
        )) {
            throw new DuplicateResourceException(
                    "Vehicle already exists with registration number: "
                            + registrationNumber
            );
        }

        vehicle.setRegistrationNumber(registrationNumber);
        vehicle.setMake(request.getMake().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setManufacturingYear(request.getManufacturingYear());
        vehicle.setColor(request.getColor().trim());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setSeatCapacity(request.getSeatCapacity());

        return mapToResponse(vehicleRepository.saveAndFlush(vehicle));
    }

    public void deactivateVehicle(Long vehicleId) {
        Vehicle vehicle = findActiveVehicle(vehicleId);
        vehicle.setActive(false);
        vehicleRepository.saveAndFlush(vehicle);
    }

    private Vehicle findActiveVehicle(Long vehicleId) {
        return vehicleRepository.findByIdAndActiveTrue(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active vehicle not found with ID: " + vehicleId
                ));
    }

    private String normalizeRegistrationNumber(String registrationNumber) {
        return registrationNumber.trim().toUpperCase(Locale.ROOT);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriver().getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .manufacturingYear(vehicle.getManufacturingYear())
                .color(vehicle.getColor())
                .vehicleType(vehicle.getVehicleType())
                .seatCapacity(vehicle.getSeatCapacity())
                .active(vehicle.isActive())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}