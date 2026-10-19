package com.ridelink.RideLink.account;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountAuthorization")
public class AccountAuthorization {

    private final AccountRepository accountRepository;

    public AccountAuthorization(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean isOwner(Long accountId, Authentication authentication) {
        if (accountId == null
                || authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        return accountRepository.findById(accountId)
                .map(account ->
                        account.getEmail().equalsIgnoreCase(
                                authentication.getName()
                        )
                )
                .orElse(false);
    }
}