package com.ridelink.RideLink.account;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An account already exists with email: " + email);
    }
}