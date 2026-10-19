package com.ridelink.RideLink.account;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Account not found with ID: " + id);
    }
}