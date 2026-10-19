package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverResponse> registerDriver(
            @Valid @RequestBody DriverCreateRequest request) {

        DriverResponse response = driverService.registerDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> findAvailableDrivers(
            @RequestParam String serviceArea) {

        return ResponseEntity.ok(
                driverService.findAvailableDrivers(serviceArea)
        );
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable Long driverId) {

        return ResponseEntity.ok(
                driverService.getDriverById(driverId)
        );
    }

    @PutMapping("/{driverId}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long driverId,
            @Valid @RequestBody DriverUpdateRequest request) {

        return ResponseEntity.ok(
                driverService.updateDriver(driverId, request)
        );
    }

    @PatchMapping("/{driverId}/availability")
    public ResponseEntity<DriverResponse> updateAvailability(
            @PathVariable Long driverId,
            @Valid @RequestBody AvailabilityUpdateRequest request) {

        return ResponseEntity.ok(
                driverService.updateAvailability(driverId, request)
        );
    }

    @PatchMapping("/{driverId}/location")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable Long driverId,
            @Valid @RequestBody LocationUpdateRequest request) {

        return ResponseEntity.ok(
                driverService.updateLocation(driverId, request)
        );
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deactivateDriver(
            @PathVariable Long driverId) {

        driverService.deactivateDriver(driverId);
        return ResponseEntity.noContent().build();
    }
}