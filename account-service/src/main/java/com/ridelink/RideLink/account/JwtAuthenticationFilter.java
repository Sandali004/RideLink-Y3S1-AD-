package com.ridelink.RideLink.account;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AccountRepository accountRepository) {

        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.validateToken(token)) {

            String email = jwtService.extractEmail(token);

            accountRepository.findByEmailIgnoreCase(email)
                    .filter(account ->
                            "ACTIVE".equalsIgnoreCase(account.getStatus()))
                    .filter(account -> account.getRole() != null)
                    .ifPresent(account -> {

                        String authorityName =
                                "ROLE_" + account.getRole()
                                        .toUpperCase(Locale.ROOT);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        account.getEmail(),
                                        null,
                                        List.of(
                                                new SimpleGrantedAuthority(
                                                        authorityName
                                                )
                                        )
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    });
        }

        filterChain.doFilter(request, response);
    }
}