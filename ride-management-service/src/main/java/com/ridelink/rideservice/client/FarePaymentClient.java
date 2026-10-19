package com.ridelink.rideservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FarePaymentClient {

    private final RestClient restClient;

    public FarePaymentClient(
            @Value("${services.fare-payment.base-url}")
            String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public double calculateFinalFare(
            String rideId,
            double distanceKm) {

        if (rideId == null || rideId.isBlank()) {
            throw new IllegalStateException("Ride ID is required");
        }

        if (distanceKm < 0.1) {
            throw new IllegalStateException(
                    "Distance must be at least 0.1 km"
            );
        }

        FareDetails fare;

        try {
            fare = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/fares/final")
                            .queryParam("rideId", rideId)
                            .queryParam("distanceKm", distanceKm)
                            .build())
                    .retrieve()
                    .body(FareDetails.class);
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Unable to calculate fare with Fare & Payment Service"
            );
        }

        if (fare == null) {
            throw new IllegalStateException(
                    "Fare information was not returned"
            );
        }

        return fare.totalFare();
    }

    private record FareDetails(double totalFare) {
    }
}