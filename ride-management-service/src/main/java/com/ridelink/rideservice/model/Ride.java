package com.ridelink.rideservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "rides")
public class Ride {

    @Id
    private String id;

    private RideStatus status;
    private String passengerId;
    private String driverId;
    private String pickup;
    private String destination;

    private Double distanceKm;
    private Double finalFare;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}