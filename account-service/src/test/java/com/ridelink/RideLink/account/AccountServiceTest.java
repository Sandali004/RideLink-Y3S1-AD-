package com.ridelink.RideLink.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(
                accountRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void createAccountShouldSaveAccountSuccessfully() {
        AccountRegistrationRequest request =
                new AccountRegistrationRequest();

        request.setName("Test Passenger");
        request.setEmail("TEST@example.com");
        request.setPassword("password123");

        when(accountRepository.existsByEmailIgnoreCase(
                "test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {

                    Account account =
                            invocation.getArgument(0);

                    account.setId(1L);
                    return account;
                });

        AccountResponse response =
                accountService.createAccount(request);

        assertEquals(1L, response.getId());
        assertEquals(
                "Test Passenger",
                response.getName()
        );
        assertEquals(
                "test@example.com",
                response.getEmail()
        );
        assertEquals(
                "PASSENGER",
                response.getRole()
        );
        assertEquals(
                "ACTIVE",
                response.getStatus()
        );

        verify(accountRepository)
                .save(any(Account.class));
    }

    @Test
    void createAccountShouldRejectDuplicateEmail() {
        AccountRegistrationRequest request =
                new AccountRegistrationRequest();

        request.setName("Duplicate User");
        request.setEmail("duplicate@example.com");
        request.setPassword("password123");

        when(accountRepository.existsByEmailIgnoreCase(
                "duplicate@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> accountService.createAccount(request)
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void loginShouldReturnJwtForValidCredentials() {
        Account account = createAccountEntity();

        LoginRequest request = new LoginRequest();
        request.setEmail("PASSENGER@example.com");
        request.setPassword("password123");

        when(accountRepository.findByEmailIgnoreCase(
                "passenger@example.com"))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "password123",
                "hashed-password"))
                .thenReturn(true);

        when(jwtService.generateToken(account))
                .thenReturn("test-jwt-token");

        LoginResponse response =
                accountService.login(request);

        assertEquals(
                "test-jwt-token",
                response.getToken()
        );
        assertEquals(
                "passenger@example.com",
                response.getEmail()
        );
    }

    @Test
    void loginShouldRejectIncorrectPassword() {
        Account account = createAccountEntity();

        LoginRequest request = new LoginRequest();
        request.setEmail("passenger@example.com");
        request.setPassword("wrong-password");

        when(accountRepository.findByEmailIgnoreCase(
                "passenger@example.com"))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> accountService.login(request)
        );
    }

    @Test
    void getAccountShouldThrowExceptionWhenMissing() {
        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById(99L)
        );
    }

    @Test
    void updateAccountStatusShouldDeactivateAccount() {
        Account account = createAccountEntity();

        AccountStatusUpdateRequest request =
                new AccountStatusUpdateRequest(
                        "inactive"
                );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(account))
                .thenReturn(account);

        AccountResponse response =
                accountService.updateAccountStatus(
                        1L,
                        request
                );

        assertEquals(
                "INACTIVE",
                response.getStatus()
        );

        verify(accountRepository).save(account);
    }

    @Test
    void updateAccountStatusShouldRejectMissingAccount() {
        AccountStatusUpdateRequest request =
                new AccountStatusUpdateRequest(
                        "inactive"
                );

        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.updateAccountStatus(
                        99L,
                        request
                )
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void deleteAccountShouldDeleteExistingAccount() {
        Account account = createAccountEntity();

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        accountService.deleteAccount(1L);

        verify(accountRepository).delete(account);
    }

    private Account createAccountEntity() {
        Account account = new Account();
        account.setId(1L);
        account.setName("Test Passenger");
        account.setEmail("passenger@example.com");
        account.setPassword("hashed-password");
        account.setRole("PASSENGER");
        account.setStatus("ACTIVE");

        return account;
    }
}