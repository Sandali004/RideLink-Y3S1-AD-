package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private Long id;
    private Long driverId;
    private String registrationNumber;
    private String make;
    private String model;
    private Integer manufacturingYear;
    private String color;
    private VehicleType vehicleType;
    private Integer seatCapacity;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}