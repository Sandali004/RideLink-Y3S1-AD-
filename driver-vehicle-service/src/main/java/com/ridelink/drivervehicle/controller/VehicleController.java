package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.VehicleCreateRequest;
import com.ridelink.drivervehicle.dto.VehicleResponse;
import com.ridelink.drivervehicle.dto.VehicleUpdateRequest;
import com.ridelink.drivervehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> registerVehicle(
            @Valid @RequestBody VehicleCreateRequest request
    ) {
        VehicleResponse response = vehicleService.registerVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> getVehicleById(
            @PathVariable Long vehicleId
    ) {
        return ResponseEntity.ok(vehicleService.getVehicleById(vehicleId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByDriver(
            @PathVariable Long driverId
    ) {
        return ResponseEntity.ok(
                vehicleService.getVehiclesByDriver(driverId)
        );
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleUpdateRequest request
    ) {
        return ResponseEntity.ok(
                vehicleService.updateVehicle(vehicleId, request)
        );
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deactivateVehicle(
            @PathVariable Long vehicleId
    ) {
        vehicleService.deactivateVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }
}