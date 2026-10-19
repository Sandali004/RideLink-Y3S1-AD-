package com.ridelink.RideLink.account;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        return toResponse(findAccount(id));
    }

    public AccountResponse createAccount(
            AccountRegistrationRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String requestedRole = request.getRole();

        String role = requestedRole == null || requestedRole.isBlank()
                ? "PASSENGER"
                : requestedRole.trim().toUpperCase(Locale.ROOT);

        Account account = new Account();
        account.setName(request.getName().trim());
        account.setEmail(normalizedEmail);
        account.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        account.setRole(role);
        account.setStatus("ACTIVE");

        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        Account account = accountRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword())) {

            throw new InvalidCredentialsException();
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                account.getId(),
                account.getName(),
                account.getEmail(),
                account.getRole(),
                account.getStatus(),
                jwtService.generateToken(account)
        );
    }

    public AccountResponse updateAccount(
            Long id,
            AccountUpdateRequest request) {

        Account account = findAccount(id);

        if (request.getName() != null
                && !request.getName().isBlank()) {

            account.setName(request.getName().trim());
        }

        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {

            String normalizedEmail =
                    normalizeEmail(request.getEmail());

            if (!normalizedEmail.equalsIgnoreCase(account.getEmail())
                    && accountRepository
                    .existsByEmailIgnoreCase(normalizedEmail)) {

                throw new EmailAlreadyExistsException(normalizedEmail);
            }

            account.setEmail(normalizedEmail);
        }

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            account.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        return toResponse(accountRepository.save(account));
    }

    public AccountResponse updateAccountStatus(
            Long id,
            AccountStatusUpdateRequest request) {

        Account account = findAccount(id);

        String normalizedStatus = request.getStatus()
                .trim()
                .toUpperCase(Locale.ROOT);

        account.setStatus(normalizedStatus);

        return toResponse(accountRepository.save(account));
    }

    public void deleteAccount(Long id) {
        Account account = findAccount(id);
        accountRepository.delete(account);
    }

    private Account findAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(
                        () -> new AccountNotFoundException(id)
                );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getEmail(),
                account.getRole(),
                account.getStatus()
        );
    }
}