package com.ridelink.RideLink.account;

public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(
            Long id,
            String name,
            String email,
            String role,
            String status,
            String token) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }
}
