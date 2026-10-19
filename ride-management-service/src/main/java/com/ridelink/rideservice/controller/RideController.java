package com.ridelink.rideservice.controller;

import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<Ride> createRide(
            @Valid @RequestBody CreateRideRequest request) {

        Ride createdRide = rideService.createRide(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRide);
    }

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                rideService.getRideById(id)
        );
    }

    @PutMapping("/{rideId}/assign-driver/{driverId}")
    public ResponseEntity<Ride> assignDriver(
            @PathVariable String rideId,
            @PathVariable String driverId) {

        return ResponseEntity.ok(
                rideService.assignDriver(rideId, driverId)
        );
    }

    @PutMapping("/{rideId}/accept")
    public ResponseEntity<Ride> acceptRide(
            @PathVariable String rideId) {

        return ResponseEntity.ok(
                rideService.acceptRide(rideId)
        );
    }

    @PutMapping("/{rideId}/start")
    public ResponseEntity<Ride> startRide(
            @PathVariable String rideId) {

        return ResponseEntity.ok(
                rideService.startRide(rideId)
        );
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<Ride> completeRide(
            @PathVariable String rideId,
            @RequestParam double distanceKm) {

        return ResponseEntity.ok(
                rideService.completeRide(rideId, distanceKm)
        );
    }

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<Ride> cancelRide(
            @PathVariable String rideId) {

        return ResponseEntity.ok(
                rideService.cancelRide(rideId)
        );
    }
}