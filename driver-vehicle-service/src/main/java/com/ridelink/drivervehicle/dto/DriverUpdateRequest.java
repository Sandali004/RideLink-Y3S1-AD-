package com.ridelink.drivervehicle.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverUpdateRequest {

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
}