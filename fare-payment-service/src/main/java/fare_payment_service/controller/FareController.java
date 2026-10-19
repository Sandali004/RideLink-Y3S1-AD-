package fare_payment_service.controller;

import fare_payment_service.entity.Fare;
import fare_payment_service.service.FareService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    public Fare estimateFare(
            @RequestParam
            @NotBlank(message = "Ride ID is required")
            String rideId,

            @RequestParam
            @DecimalMin(
                    value = "0.1",
                    message = "Distance must be at least 0.1 km"
            )
            double distanceKm) {

        return fareService.calculateFare(rideId, distanceKm);
    }

    @PostMapping("/final")
    public Fare finalFare(
            @RequestParam
            @NotBlank(message = "Ride ID is required")
            String rideId,

            @RequestParam
            @DecimalMin(
                    value = "0.1",
                    message = "Distance must be at least 0.1 km"
            )
            double distanceKm) {

        return fareService.calculateFare(rideId, distanceKm);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationException(
            ConstraintViolationException exception) {

        return ResponseEntity
                .badRequest()
                .body("Validation error: " + exception.getMessage());
    }
}