package com.usg.apiAutomation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ✅ Disable CSRF for APIs
                .csrf(AbstractHttpConfigurer::disable)

                // ✅ Disable Form Login COMPLETELY (NO HTML REDIRECTS)
                .formLogin(AbstractHttpConfigurer::disable)

                // ✅ Disable logout HTML handling
                .logout(AbstractHttpConfigurer::disable)

                // ✅ Use API-style 401 instead of HTML login page
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                new HttpStatusEntryPoint(UNAUTHORIZED)
                        )
                )

                // ✅ Hook your custom user service
                .userDetailsService(userDetailsService)

                // ✅ Authorization Rules
                .authorizeHttpRequests(auth -> auth

                        // ✅ Swagger stays public
                        .requestMatchers(
                                "/",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // ✅ ✅ ✅ YOUR TParty APIS — PUBLIC
                        .requestMatchers("/plx/api/**").permitAll()

                        // ✅ Everything else locked down
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}