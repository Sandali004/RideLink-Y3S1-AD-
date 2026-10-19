package fare_payment_service.service;

import fare_payment_service.entity.Fare;
import fare_payment_service.repository.FareRepository;
import org.springframework.stereotype.Service;

@Service
public class FareService {

    private final FareRepository fareRepository;

    public FareService(FareRepository fareRepository) {
        this.fareRepository = fareRepository;
    }

    public Fare calculateFare(String rideId, double distanceKm) {
        double baseFare = 100.0;
        double perKmRate = 80.0;
        double totalFare = baseFare + (distanceKm * perKmRate);

        Fare fare = new Fare(
                rideId,
                distanceKm,
                baseFare,
                perKmRate,
                totalFare
        );

        return fareRepository.save(fare);
    }
}