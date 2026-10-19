package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.entity.Driver;
import com.ridelink.drivervehicle.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Driver> findByActiveTrue();

    List<Driver> findByAvailabilityStatusAndServiceAreaIgnoreCaseAndActiveTrue(
            AvailabilityStatus availabilityStatus,
            String serviceArea
    );
}