package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.DriverCreateRequest;
import com.ridelink.drivervehicle.dto.DriverResponse;
import com.ridelink.drivervehicle.entity.Driver;
import com.ridelink.drivervehicle.enums.AvailabilityStatus;
import com.ridelink.drivervehicle.exception.DuplicateResourceException;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverService driverService;

    @Test
    void registerDriverShouldSaveAndReturnDriver() {
        DriverCreateRequest request = createValidRequest();

        when(driverRepository.existsByAccountId(101L))
                .thenReturn(false);

        when(driverRepository.existsByLicenseNumber("B1234"))
                .thenReturn(false);

        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation -> {
                    Driver savedDriver = invocation.getArgument(0);
                    savedDriver.setId(1L);
                    return savedDriver;
                });

        DriverResponse response = driverService.registerDriver(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getAccountId());
        assertEquals("B1234", response.getLicenseNumber());
        assertEquals("Colombo", response.getServiceArea());
        assertEquals(AvailabilityStatus.OFFLINE,
                response.getAvailabilityStatus());
        assertTrue(response.isActive());

        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void registerDriverShouldRejectDuplicateAccount() {
        DriverCreateRequest request = createValidRequest();

        when(driverRepository.existsByAccountId(101L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> driverService.registerDriver(request)
        );

        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void registerDriverShouldRejectDuplicateLicenseNumber() {
        DriverCreateRequest request = createValidRequest();

        when(driverRepository.existsByAccountId(101L))
                .thenReturn(false);

        when(driverRepository.existsByLicenseNumber("B1234"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> driverService.registerDriver(request)
        );

        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void getDriverByIdShouldRejectMissingDriver() {
        when(driverRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.getDriverById(99L)
        );
    }

    @Test
    void findAvailableDriversShouldExcludeExpiredLicenses() {
        Driver validDriver = createDriver(
                1L,
                LocalDate.now().plusYears(1)
        );

        Driver expiredDriver = createDriver(
                2L,
                LocalDate.now().minusDays(1)
        );

        when(driverRepository
                .findByAvailabilityStatusAndServiceAreaIgnoreCaseAndActiveTrue(
                        AvailabilityStatus.AVAILABLE,
                        "Colombo"
                ))
                .thenReturn(List.of(validDriver, expiredDriver));

        when(vehicleRepository.existsByDriver_IdAndActiveTrue(1L))
                .thenReturn(true);

        List<DriverResponse> results =
                driverService.findAvailableDrivers("Colombo");

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());

        verify(vehicleRepository)
                .existsByDriver_IdAndActiveTrue(1L);

        verify(vehicleRepository, never())
                .existsByDriver_IdAndActiveTrue(2L);
    }

    @Test
    void deactivateDriverShouldMakeDriverInactiveAndOffline() {
        Driver driver = createDriver(
                1L,
                LocalDate.now().plusYears(1)
        );

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        driverService.deactivateDriver(1L);

        assertFalse(driver.isActive());
        assertEquals(
                AvailabilityStatus.OFFLINE,
                driver.getAvailabilityStatus()
        );

        verify(driverRepository).saveAndFlush(driver);
    }

    private DriverCreateRequest createValidRequest() {
        DriverCreateRequest request = new DriverCreateRequest();

        request.setAccountId(101L);
        request.setLicenseNumber(" B1234 ");
        request.setLicenseExpiryDate(LocalDate.now().plusYears(2));
        request.setYearsOfExperience(5);
        request.setServiceArea(" Colombo ");
        request.setCurrentLatitude(null);
        request.setCurrentLongitude(null);

        return request;
    }

    private Driver createDriver(
            Long id,
            LocalDate licenseExpiryDate
    ) {
        return Driver.builder()
                .id(id)
                .accountId(1000L + id)
                .licenseNumber("LICENSE-" + id)
                .licenseExpiryDate(licenseExpiryDate)
                .yearsOfExperience(5)
                .serviceArea("Colombo")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .active(true)
                .build();
    }
}