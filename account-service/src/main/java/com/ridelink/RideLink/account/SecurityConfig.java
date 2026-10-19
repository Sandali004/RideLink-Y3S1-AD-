package com.ridelink.RideLink.account;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Locale;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public UserDetailsService userDetailsService(
            AccountRepository accountRepository) {

        return email -> {

            Account account = accountRepository
                    .findByEmailIgnoreCase(email)
                    .orElseThrow(
                            () -> new UsernameNotFoundException(
                                    "Account not found"
                            )
                    );

            if (account.getRole() == null
                    || account.getRole().isBlank()) {

                throw new UsernameNotFoundException(
                        "Account role is missing"
                );
            }

            String role = account.getRole()
                    .toUpperCase(Locale.ROOT);

            return User.withUsername(account.getEmail())
                    .password(account.getPassword())
                    .authorities("ROLE_" + role)
                    .disabled(
                            !"ACTIVE".equalsIgnoreCase(
                                    account.getStatus()
                            )
                    )
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize -> authorize

                        // Public registration and login
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/accounts",
                                "/api/accounts/login"
                        ).permitAll()

                        // Public Swagger/OpenAPI pages
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()

                        // Admin-only operations
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/accounts"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/accounts/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/accounts/*/status"
                        ).hasRole("ADMIN")

                        // Other endpoints require a valid JWT
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exceptions -> exceptions

                        .authenticationEntryPoint(
                                (request, response, exception) -> {

                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    );

                                    response.setContentType(
                                            MediaType.APPLICATION_JSON_VALUE
                                    );

                                    response.getWriter().write(
                                            """
                                            {
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "A valid JWT token is required"
                                            }
                                            """
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (request, response, exception) -> {

                                    response.setStatus(
                                            HttpServletResponse.SC_FORBIDDEN
                                    );

                                    response.setContentType(
                                            MediaType.APPLICATION_JSON_VALUE
                                    );

                                    response.getWriter().write(
                                            """
                                            {
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "You do not have permission to perform this action"
                                            }
                                            """
                                    );
                                }
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}