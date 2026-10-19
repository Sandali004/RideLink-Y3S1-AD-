package com.ridelink.rideservice.service;

import com.ridelink.rideservice.client.DriverVehicleClient;
import com.ridelink.rideservice.client.FarePaymentClient;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.exception.RideNotFoundException;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverVehicleClient driverVehicleClient;
    private final FarePaymentClient farePaymentClient;

    public RideService(
            RideRepository rideRepository,
            DriverVehicleClient driverVehicleClient,
            FarePaymentClient farePaymentClient) {

        this.rideRepository = rideRepository;
        this.driverVehicleClient = driverVehicleClient;
        this.farePaymentClient = farePaymentClient;
    }

    public Ride createRide(CreateRideRequest request) {

        Ride ride = new Ride();

        ride.setPassengerId(request.getPassengerId());
        ride.setPickup(request.getPickup());
        ride.setDestination(request.getDestination());
        ride.setStatus(RideStatus.REQUESTED);

        LocalDateTime now = LocalDateTime.now();
        ride.setCreatedAt(now);
        ride.setUpdatedAt(now);

        return rideRepository.save(ride);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Ride getRideById(String id) {
        return findRide(id);
    }

    public Ride assignDriver(
            String rideId,
            String driverId) {

        Ride ride = findRide(rideId);

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Driver can only be assigned to a REQUESTED ride"
            );
        }

        driverVehicleClient.ensureDriverIsAvailable(driverId);

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public Ride acceptRide(String rideId) {

        Ride ride = findRide(rideId);

        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Only an ASSIGNED ride can be accepted"
            );
        }

        ride.setStatus(RideStatus.ACCEPTED);
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public Ride startRide(String rideId) {

        Ride ride = findRide(rideId);

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Only an ACCEPTED ride can be started"
            );
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public Ride completeRide(
            String rideId,
            double distanceKm) {

        Ride ride = findRide(rideId);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only an IN_PROGRESS ride can be completed"
            );
        }

        if (distanceKm < 0.1) {
            throw new IllegalStateException(
                    "Distance must be at least 0.1 km"
            );
        }

        double finalFare =
                farePaymentClient.calculateFinalFare(
                        ride.getId(),
                        distanceKm
                );

        ride.setDistanceKm(distanceKm);
        ride.setFinalFare(finalFare);
        ride.setStatus(RideStatus.COMPLETED);
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public Ride cancelRide(String rideId) {

        Ride ride = findRide(rideId);

        if (ride.getStatus() != RideStatus.REQUESTED
                && ride.getStatus() != RideStatus.ASSIGNED) {

            throw new IllegalStateException(
                    "Only REQUESTED or ASSIGNED rides can be cancelled"
            );
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setUpdatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    private Ride findRide(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException("Ride not found")
                );
    }
}