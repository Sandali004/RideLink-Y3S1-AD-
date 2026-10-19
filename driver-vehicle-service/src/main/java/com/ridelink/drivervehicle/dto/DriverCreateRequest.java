package com.ridelink.drivervehicle.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverCreateRequest {

    @NotNull(message = "Account ID is required")
    @Positive(message = "Account ID must be positive")
    private Long accountId;

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    private String licenseNumber;

    @NotNull(message = "License expiry date is required")
    @FutureOrPresent(message = "License must not be expired")
    private LocalDate licenseExpiryDate;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 60, message = "Experience cannot exceed 60 years")
    private Integer yearsOfExperience;

    @NotBlank(message = "Service area is required")
    @Size(max = 100, message = "Service area cannot exceed 100 characters")
    private String serviceArea;

    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Latitude must not exceed 90")
    private BigDecimal currentLatitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Longitude must not exceed 180")
    private BigDecimal currentLongitude;
}