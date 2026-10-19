package com.ridelink.rideservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DriverVehicleClient {

    private final RestClient restClient;

    public DriverVehicleClient(
            @Value("${services.driver-vehicle.base-url}")
            String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void ensureDriverIsAvailable(String driverId) {

        if (driverId == null || driverId.isBlank()) {
            throw new IllegalStateException("Driver ID is required");
        }

        DriverDetails driver;

        try {
            driver = restClient.get()
                    .uri("/api/drivers/{driverId}", driverId)
                    .retrieve()
                    .body(DriverDetails.class);
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Unable to validate driver with Driver & Vehicle Service"
            );
        }

        if (driver == null) {
            throw new IllegalStateException(
                    "Driver information was not returned"
            );
        }

        if (!driver.active()) {
            throw new IllegalStateException(
                    "The selected driver is not active"
            );
        }

        if (!"AVAILABLE".equalsIgnoreCase(
                driver.availabilityStatus())) {

            throw new IllegalStateException(
                    "The selected driver is not available"
            );
        }
    }

    private record DriverDetails(
            boolean active,
            String availabilityStatus) {
    }
}