package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.enums.VehicleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUpdateRequest {

    @NotBlank(message = "Registration number is required")
    @Size(max = 20, message = "Registration number cannot exceed 20 characters")
    private String registrationNumber;

    @NotBlank(message = "Vehicle make is required")
    @Size(max = 50, message = "Vehicle make cannot exceed 50 characters")
    private String make;

    @NotBlank(message = "Vehicle model is required")
    @Size(max = 50, message = "Vehicle model cannot exceed 50 characters")
    private String model;

    @NotNull(message = "Manufacturing year is required")
    @Min(value = 1990, message = "Manufacturing year must be 1990 or later")
    @Max(value = 2100, message = "Manufacturing year is invalid")
    private Integer manufacturingYear;

    @NotBlank(message = "Vehicle color is required")
    @Size(max = 30, message = "Vehicle color cannot exceed 30 characters")
    private String color;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Seat capacity is required")
    @Min(value = 1, message = "Seat capacity must be at least 1")
    @Max(value = 20, message = "Seat capacity cannot exceed 20")
    private Integer seatCapacity;
}