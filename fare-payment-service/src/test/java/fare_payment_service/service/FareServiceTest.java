package fare_payment_service.service;

import fare_payment_service.entity.Fare;
import fare_payment_service.repository.FareRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FareServiceTest {

    @Test
    void calculateFareShouldCalculateCorrectTotal() {
        FareRepository fareRepository = mock(FareRepository.class);
        FareService fareService = new FareService(fareRepository);

        Fare savedFare = new Fare(
                "RIDE-001",
                5.0,
                100.0,
                80.0,
                500.0
        );

        when(fareRepository.save(any(Fare.class)))
                .thenReturn(savedFare);

        Fare result = fareService.calculateFare("RIDE-001", 5.0);

        assertEquals("RIDE-001", result.getRideId());
        assertEquals(500.0, result.getTotalFare());
        assertEquals(5.0, result.getDistanceKm());
        assertEquals(100.0, result.getBaseFare());
        assertEquals(80.0, result.getPerKmRate());

        verify(fareRepository).save(any(Fare.class));
    }

    @Test
    void calculateFareShouldSupportMinimumAllowedDistance() {
        FareRepository fareRepository = mock(FareRepository.class);
        FareService fareService = new FareService(fareRepository);

        when(fareRepository.save(any(Fare.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Fare result = fareService.calculateFare("RIDE-002", 0.1);

        assertEquals("RIDE-002", result.getRideId());
        assertEquals(0.1, result.getDistanceKm(), 0.001);
        assertEquals(108.0, result.getTotalFare(), 0.001);

        verify(fareRepository).save(any(Fare.class));
    }
}