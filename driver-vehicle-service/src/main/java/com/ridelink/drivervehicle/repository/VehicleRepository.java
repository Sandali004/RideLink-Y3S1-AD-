package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    boolean existsByRegistrationNumberIgnoreCaseAndIdNot(
            String registrationNumber,
            Long vehicleId
    );

    Optional<Vehicle> findByIdAndActiveTrue(Long vehicleId);

    List<Vehicle> findByActiveTrue();

    List<Vehicle> findByDriver_IdAndActiveTrue(Long driverId);

    boolean existsByDriver_IdAndActiveTrue(Long driverId);
}