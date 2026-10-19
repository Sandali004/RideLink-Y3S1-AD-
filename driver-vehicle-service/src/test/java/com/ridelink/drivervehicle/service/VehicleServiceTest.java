package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.VehicleCreateRequest;
import com.ridelink.drivervehicle.dto.VehicleResponse;
import com.ridelink.drivervehicle.entity.Driver;
import com.ridelink.drivervehicle.entity.Vehicle;
import com.ridelink.drivervehicle.enums.VehicleType;
import com.ridelink.drivervehicle.exception.DuplicateResourceException;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void registerVehicleShouldSaveAndReturnVehicle() {
        Driver driver = createActiveDriver();
        VehicleCreateRequest request = createValidRequest();

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository
                .existsByRegistrationNumberIgnoreCase("CAB-1234"))
                .thenReturn(false);

        when(vehicleRepository.saveAndFlush(any(Vehicle.class)))
                .thenAnswer(invocation -> {
                    Vehicle savedVehicle = invocation.getArgument(0);
                    savedVehicle.setId(10L);
                    return savedVehicle;
                });

        VehicleResponse response =
                vehicleService.registerVehicle(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getDriverId());
        assertEquals("CAB-1234", response.getRegistrationNumber());
        assertEquals("Toyota", response.getMake());
        assertEquals("Prius", response.getModel());
        assertEquals(VehicleType.CAR, response.getVehicleType());
        assertTrue(response.isActive());

        verify(vehicleRepository)
                .saveAndFlush(any(Vehicle.class));
    }

    @Test
    void registerVehicleShouldRejectInactiveDriver() {
        Driver inactiveDriver = createActiveDriver();
        inactiveDriver.setActive(false);

        VehicleCreateRequest request = createValidRequest();

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(inactiveDriver));

        assertThrows(
                ResourceNotFoundException.class,
                () -> vehicleService.registerVehicle(request)
        );

        verify(vehicleRepository, never())
                .saveAndFlush(any(Vehicle.class));

        verify(vehicleRepository, never())
                .existsByRegistrationNumberIgnoreCase(anyString());
    }

    @Test
    void registerVehicleShouldRejectDuplicateRegistrationNumber() {
        Driver driver = createActiveDriver();
        VehicleCreateRequest request = createValidRequest();

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository
                .existsByRegistrationNumberIgnoreCase("CAB-1234"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> vehicleService.registerVehicle(request)
        );

        verify(vehicleRepository, never())
                .saveAndFlush(any(Vehicle.class));
    }

    @Test
    void getVehicleByIdShouldRejectMissingVehicle() {
        when(vehicleRepository.findByIdAndActiveTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> vehicleService.getVehicleById(99L)
        );
    }

    @Test
    void deactivateVehicleShouldMakeVehicleInactive() {
        Driver driver = createActiveDriver();
        Vehicle vehicle = createActiveVehicle(driver);

        when(vehicleRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.of(vehicle));

        vehicleService.deactivateVehicle(10L);

        assertFalse(vehicle.isActive());

        verify(vehicleRepository).saveAndFlush(vehicle);
    }

    @Test
    void getVehiclesByDriverShouldRejectMissingDriver() {
        when(driverRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> vehicleService.getVehiclesByDriver(99L)
        );

        verify(vehicleRepository, never())
                .findByDriver_IdAndActiveTrue(99L);
    }

    private VehicleCreateRequest createValidRequest() {
        VehicleCreateRequest request =
                new VehicleCreateRequest();

        request.setDriverId(1L);
        request.setRegistrationNumber(" cab-1234 ");
        request.setMake(" Toyota ");
        request.setModel(" Prius ");
        request.setManufacturingYear(2022);
        request.setColor(" Black ");
        request.setVehicleType(VehicleType.CAR);
        request.setSeatCapacity(4);

        return request;
    }

    private Driver createActiveDriver() {
        return Driver.builder()
                .id(1L)
                .active(true)
                .build();
    }

    private Vehicle createActiveVehicle(Driver driver) {
        return Vehicle.builder()
                .id(10L)
                .driver(driver)
                .registrationNumber("CAB-1234")
                .make("Toyota")
                .model("Prius")
                .manufacturingYear(2022)
                .color("Black")
                .vehicleType(VehicleType.CAR)
                .seatCapacity(4)
                .active(true)
                .build();
    }
}