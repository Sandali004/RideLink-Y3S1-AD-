package com.ridelink.rideservice;

import com.ridelink.rideservice.client.DriverVehicleClient;
import com.ridelink.rideservice.client.FarePaymentClient;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.exception.RideNotFoundException;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.repository.RideRepository;
import com.ridelink.rideservice.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverVehicleClient driverVehicleClient;

    @Mock
    private FarePaymentClient farePaymentClient;

    @InjectMocks
    private RideService rideService;

    private CreateRideRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateRideRequest();
        request.setPassengerId("P001");
        request.setPickup("Malabe");
        request.setDestination("Colombo");
    }

    @Test
    void createRideShouldCreateRideWithRequestedStatus() {

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.createRide(request);

        assertEquals("P001", result.getPassengerId());
        assertEquals("Malabe", result.getPickup());
        assertEquals("Colombo", result.getDestination());
        assertEquals(RideStatus.REQUESTED, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void getRideByIdShouldReturnExistingRide() {

        Ride ride = createRide("R001", RideStatus.REQUESTED);

        when(rideRepository.findById("R001"))
                .thenReturn(Optional.of(ride));

        Ride result = rideService.getRideById("R001");

        assertEquals("R001", result.getId());
        assertEquals(RideStatus.REQUESTED, result.getStatus());
    }

    @Test
    void getRideByIdShouldThrowWhenRideDoesNotExist() {

        when(rideRepository.findById("R999"))
                .thenReturn(Optional.empty());

        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> rideService.getRideById("R999")
        );

        assertEquals("Ride not found", exception.getMessage());
    }

    @Test
    void assignDriverShouldValidateDriverAndAssignRide() {

        Ride ride = createRide("R001", RideStatus.REQUESTED);

        when(rideRepository.findById("R001"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.assignDriver("R001", "1");

        assertEquals("1", result.getDriverId());
        assertEquals(RideStatus.ASSIGNED, result.getStatus());

        verify(driverVehicleClient)
                .ensureDriverIsAvailable("1");

        verify(rideRepository).save(ride);
    }

    @Test
    void assignDriverShouldRejectRideThatIsNotRequested() {

        Ride ride = createRide("R002", RideStatus.CANCELLED);

        when(rideRepository.findById("R002"))
                .thenReturn(Optional.of(ride));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.assignDriver("R002", "2")
        );

        assertEquals(
                "Driver can only be assigned to a REQUESTED ride",
                exception.getMessage()
        );

        verifyNoInteractions(driverVehicleClient);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void assignDriverShouldRejectUnavailableDriver() {

        Ride ride = createRide("R003", RideStatus.REQUESTED);

        when(rideRepository.findById("R003"))
                .thenReturn(Optional.of(ride));

        doThrow(new IllegalStateException(
                "The selected driver is not available"
        )).when(driverVehicleClient)
                .ensureDriverIsAvailable("3");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.assignDriver("R003", "3")
        );

        assertEquals(
                "The selected driver is not available",
                exception.getMessage()
        );

        assertEquals(RideStatus.REQUESTED, ride.getStatus());
        assertNull(ride.getDriverId());

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void acceptRideShouldChangeAssignedRideToAccepted() {

        Ride ride = createRide("R004", RideStatus.ASSIGNED);
        ride.setDriverId("4");

        when(rideRepository.findById("R004"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.acceptRide("R004");

        assertEquals(RideStatus.ACCEPTED, result.getStatus());
        assertEquals("4", result.getDriverId());
    }

    @Test
    void startRideShouldChangeAcceptedRideToInProgress() {

        Ride ride = createRide("R005", RideStatus.ACCEPTED);
        ride.setDriverId("5");

        when(rideRepository.findById("R005"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.startRide("R005");

        assertEquals(RideStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void completeRideShouldCalculateFareAndCompleteRide() {

        Ride ride = createRide("R006", RideStatus.IN_PROGRESS);
        ride.setDriverId("6");

        when(rideRepository.findById("R006"))
                .thenReturn(Optional.of(ride));

        when(farePaymentClient.calculateFinalFare(
                "R006",
                5.0
        )).thenReturn(500.0);

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.completeRide("R006", 5.0);

        assertEquals(RideStatus.COMPLETED, result.getStatus());
        assertEquals(5.0, result.getDistanceKm(), 0.001);
        assertEquals(500.0, result.getFinalFare(), 0.001);

        verify(farePaymentClient)
                .calculateFinalFare("R006", 5.0);

        verify(rideRepository).save(ride);
    }

    @Test
    void completeRideShouldRejectRideThatIsNotInProgress() {

        Ride ride = createRide("R007", RideStatus.ACCEPTED);

        when(rideRepository.findById("R007"))
                .thenReturn(Optional.of(ride));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.completeRide("R007", 5.0)
        );

        assertEquals(
                "Only an IN_PROGRESS ride can be completed",
                exception.getMessage()
        );

        verifyNoInteractions(farePaymentClient);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void completeRideShouldRejectInvalidDistance() {

        Ride ride = createRide("R008", RideStatus.IN_PROGRESS);

        when(rideRepository.findById("R008"))
                .thenReturn(Optional.of(ride));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.completeRide("R008", 0.0)
        );

        assertEquals(
                "Distance must be at least 0.1 km",
                exception.getMessage()
        );

        verifyNoInteractions(farePaymentClient);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void completeRideShouldRemainInProgressWhenFareServiceFails() {

        Ride ride = createRide("R009", RideStatus.IN_PROGRESS);

        when(rideRepository.findById("R009"))
                .thenReturn(Optional.of(ride));

        when(farePaymentClient.calculateFinalFare(
                "R009",
                7.5
        )).thenThrow(new IllegalStateException(
                "Unable to calculate fare with Fare & Payment Service"
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.completeRide("R009", 7.5)
        );

        assertEquals(
                "Unable to calculate fare with Fare & Payment Service",
                exception.getMessage()
        );

        assertEquals(RideStatus.IN_PROGRESS, ride.getStatus());
        assertNull(ride.getDistanceKm());
        assertNull(ride.getFinalFare());

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void cancelRideShouldCancelRequestedRide() {

        Ride ride = createRide("R010", RideStatus.REQUESTED);

        when(rideRepository.findById("R010"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.cancelRide("R010");

        assertEquals(RideStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelRideShouldCancelAssignedRide() {

        Ride ride = createRide("R011", RideStatus.ASSIGNED);
        ride.setDriverId("11");

        when(rideRepository.findById("R011"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.cancelRide("R011");

        assertEquals(RideStatus.CANCELLED, result.getStatus());
        assertEquals("11", result.getDriverId());
    }

    private Ride createRide(
            String id,
            RideStatus status) {

        Ride ride = new Ride();
        ride.setId(id);
        ride.setPassengerId("P001");
        ride.setPickup("Malabe");
        ride.setDestination("Colombo");
        ride.setStatus(status);

        return ride;
    }
}