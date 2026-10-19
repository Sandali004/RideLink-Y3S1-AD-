package com.ridelink.rideservice.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class CreateRideRequest {
    @NotBlank(message = "Passenger ID is required")
private String passengerId;
@NotBlank(message = "Pickup location is required")
private String pickup;
@NotBlank(message = "Destination is required")
private String destination;
}
