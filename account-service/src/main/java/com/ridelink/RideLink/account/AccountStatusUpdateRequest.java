package com.ridelink.RideLink.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AccountStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "(?i)^(ACTIVE|INACTIVE)$",
            message = "Status must be ACTIVE or INACTIVE"
    )
    private String status;

    public AccountStatusUpdateRequest() {
    }

    public AccountStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}