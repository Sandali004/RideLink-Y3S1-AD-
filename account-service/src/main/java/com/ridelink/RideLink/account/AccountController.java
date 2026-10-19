package com.ridelink.RideLink.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(
        name = "Account Management",
        description = "Registration, login, profile and administration endpoints"
)
public class AccountController {

    private static final String BEARER_AUTH = "bearerAuth";

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all accounts",
            description = "Returns all accounts. ADMIN role is required.",
            security = {
                    @SecurityRequirement(name = BEARER_AUTH)
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            )
    })
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
            "@accountAuthorization.isOwner(#id, authentication)"
    )
    @Operation(
            summary = "Get an account by ID",
            description = "ADMIN users or the account owner may access it.",
            security = {
                    @SecurityRequirement(name = BEARER_AUTH)
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access to this account is forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account was not found"
            )
    })
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id) {

        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping
    @Operation(
            summary = "Register an account",
            description = "Public registration for PASSENGER or DRIVER accounts."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email address is already registered"
            )
    })
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRegistrationRequest request) {

        AccountResponse createdAccount =
                accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdAccount);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in",
            description = "Authenticates an active account and returns a JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Email or password is invalid"
            )
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(accountService.login(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
            "@accountAuthorization.isOwner(#id, authentication)"
    )
    @Operation(
            summary = "Update an account",
            description = "ADMIN users or the account owner may update it.",
            security = {
                    @SecurityRequirement(name = BEARER_AUTH)
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access to this account is forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account was not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email address is already registered"
            )
    })
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {

        return ResponseEntity.ok(
                accountService.updateAccount(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Change account status",
            description = "Activates or deactivates an account. ADMIN role is required.",
            security = {
                    @SecurityRequirement(name = BEARER_AUTH)
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account status updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status must be ACTIVE or INACTIVE"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account was not found"
            )
    })
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody AccountStatusUpdateRequest request) {

        return ResponseEntity.ok(
                accountService.updateAccountStatus(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete an account",
            description = "Deletes an account. ADMIN role is required.",
            security = {
                    @SecurityRequirement(name = BEARER_AUTH)
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Account deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT token is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account was not found"
            )
    })
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id) {

        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}