package com.ridelink.RideLink.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Account passenger;
    private Account otherPassenger;
    private Account admin;

    private String passengerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        passenger = createAccount(
                "Test Passenger",
                "passenger@example.com",
                "PASSENGER"
        );

        otherPassenger = createAccount(
                "Other Passenger",
                "other@example.com",
                "PASSENGER"
        );

        admin = createAccount(
                "Test Admin",
                "admin@example.com",
                "ADMIN"
        );

        passengerToken = jwtService.generateToken(passenger);
        adminToken = jwtService.generateToken(admin);
    }

    @Test
    void registrationShouldBePublic() throws Exception {
        mockMvc.perform(
                        post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "New Passenger",
                                          "email": "new@example.com",
                                          "password": "password123"
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.email")
                                .value("new@example.com")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("PASSENGER")
                );
    }

    @Test
    void driverRegistrationShouldBeSuccessful() throws Exception {
        mockMvc.perform(
                        post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "New Driver",
                                          "email": "driver@example.com",
                                          "password": "password123",
                                          "role": "DRIVER"
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.email")
                                .value("driver@example.com")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("DRIVER")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                );
    }

    @Test
    void publicRegistrationShouldRejectAdminRole() throws Exception {
        mockMvc.perform(
                        post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "Invalid Admin",
                                          "email": "invalid-admin@example.com",
                                          "password": "password123",
                                          "role": "ADMIN"
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpointShouldRejectMissingToken()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/accounts/{id}",
                                passenger.getId()
                        )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passengerShouldAccessOwnAccount()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/accounts/{id}",
                                passenger.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(passengerToken)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.email")
                                .value("passenger@example.com")
                );
    }

    @Test
    void passengerShouldNotAccessAnotherAccount()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/accounts/{id}",
                                otherPassenger.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(passengerToken)
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void passengerShouldUpdateOwnAccount()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/accounts/{id}",
                                passenger.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(passengerToken)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "Updated Passenger"
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.name")
                                .value("Updated Passenger")
                );
    }

    @Test
    void passengerShouldNotUpdateAnotherAccount()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/accounts/{id}",
                                otherPassenger.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(passengerToken)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "Unauthorized Update"
                                        }
                                        """
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void passengerShouldBeForbiddenFromAccountList()
            throws Exception {

        mockMvc.perform(
                        get("/api/accounts")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(passengerToken)
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldAccessAccountList()
            throws Exception {

        mockMvc.perform(
                        get("/api/accounts")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(adminToken)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void passengerShouldNotChangeAccountStatus()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/accounts/{id}/status",
                                passenger.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(passengerToken)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "status": "INACTIVE"
                                        }
                                        """
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldDeactivateAccount()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/accounts/{id}/status",
                                passenger.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(adminToken)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "status": "INACTIVE"
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("INACTIVE")
                );
    }

    private Account createAccount(
            String name,
            String email,
            String role) {

        Account account = new Account();
        account.setName(name);
        account.setEmail(email);
        account.setPassword(
                passwordEncoder.encode("password123")
        );
        account.setRole(role);
        account.setStatus("ACTIVE");

        return accountRepository.saveAndFlush(account);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}