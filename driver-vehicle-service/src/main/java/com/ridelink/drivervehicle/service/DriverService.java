package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.entity.Driver;
import com.ridelink.drivervehicle.enums.AvailabilityStatus;
import com.ridelink.drivervehicle.exception.DuplicateResourceException;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverResponse registerDriver(DriverCreateRequest request) {
        if (driverRepository.existsByAccountId(request.getAccountId())) {
            throw new DuplicateResourceException(
                    "A driver already exists for account ID "
                            + request.getAccountId()
            );
        }

        String licenseNumber = request.getLicenseNumber().trim();

        if (driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new DuplicateResourceException(
                    "License number already exists: " + licenseNumber
            );
        }

        Driver driver = Driver.builder()
                .accountId(request.getAccountId())
                .licenseNumber(licenseNumber)
                .licenseExpiryDate(request.getLicenseExpiryDate())
                .yearsOfExperience(request.getYearsOfExperience())
                .serviceArea(request.getServiceArea().trim())
                .currentLatitude(request.getCurrentLatitude())
                .currentLongitude(request.getCurrentLongitude())
                .availabilityStatus(AvailabilityStatus.OFFLINE)
                .active(true)
                .build();

        return mapToResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long driverId) {
        return mapToResponse(findActiveDriver(driverId));
    }

    public DriverResponse updateDriver(
            Long driverId,
            DriverUpdateRequest request
    ) {
        Driver driver = findActiveDriver(driverId);
        String licenseNumber = request.getLicenseNumber().trim();

        if (!driver.getLicenseNumber().equalsIgnoreCase(licenseNumber)
                && driverRepository.existsByLicenseNumber(licenseNumber)) {

            throw new DuplicateResourceException(
                    "License number already exists: " + licenseNumber
            );
        }

        driver.setLicenseNumber(licenseNumber);
        driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
        driver.setYearsOfExperience(request.getYearsOfExperience());
        driver.setServiceArea(request.getServiceArea().trim());

        return mapToResponse(driverRepository.saveAndFlush(driver));
    }

    public DriverResponse updateAvailability(
            Long driverId,
            AvailabilityUpdateRequest request
    ) {
        Driver driver = findActiveDriver(driverId);
        driver.setAvailabilityStatus(request.getAvailabilityStatus());

        return mapToResponse(driverRepository.saveAndFlush(driver));
    }

    public DriverResponse updateLocation(
            Long driverId,
            LocationUpdateRequest request
    ) {
        Driver driver = findActiveDriver(driverId);
        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());

        return mapToResponse(driverRepository.saveAndFlush(driver));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAvailableDrivers(String serviceArea) {
        return driverRepository
                .findByAvailabilityStatusAndServiceAreaIgnoreCaseAndActiveTrue(
                        AvailabilityStatus.AVAILABLE,
                        serviceArea.trim()
                )
                .stream()
                .filter(driver ->
                        !driver.getLicenseExpiryDate()
                                .isBefore(LocalDate.now())
                )
                .filter(driver ->
                        vehicleRepository.existsByDriver_IdAndActiveTrue(
                                driver.getId()
                        )
                )
                .map(this::mapToResponse)
                .toList();
    }

    public void deactivateDriver(Long driverId) {
        Driver driver = findActiveDriver(driverId);
        driver.setActive(false);
        driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        driverRepository.saveAndFlush(driver);
    }

    private Driver findActiveDriver(Long driverId) {
        return driverRepository.findById(driverId)
                .filter(Driver::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver not found with ID " + driverId
                        )
                );
    }

    private DriverResponse mapToResponse(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .accountId(driver.getAccountId())
                .licenseNumber(driver.getLicenseNumber())
                .licenseExpiryDate(driver.getLicenseExpiryDate())
                .yearsOfExperience(driver.getYearsOfExperience())
                .serviceArea(driver.getServiceArea())
                .currentLatitude(driver.getCurrentLatitude())
                .currentLongitude(driver.getCurrentLongitude())
                .availabilityStatus(driver.getAvailabilityStatus())
                .active(driver.isActive())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}