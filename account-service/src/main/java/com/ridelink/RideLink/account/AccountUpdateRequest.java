package com.ridelink.RideLink.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class AccountUpdateRequest {

    @Size(min = 2, max = 100, message = "Name must contain 2 to 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    public AccountUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}