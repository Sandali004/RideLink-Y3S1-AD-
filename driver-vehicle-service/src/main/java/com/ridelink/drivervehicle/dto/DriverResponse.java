package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private Long id;
    private Long accountId;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private Integer yearsOfExperience;
    private String serviceArea;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private AvailabilityStatus availabilityStatus;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}