package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.enums.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityUpdateRequest {

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus availabilityStatus;
}