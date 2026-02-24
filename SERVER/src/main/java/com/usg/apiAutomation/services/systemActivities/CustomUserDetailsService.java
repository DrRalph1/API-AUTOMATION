package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.entities.postgres.UserEntity;
import com.usg.apiAutomation.repositories.postgres.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Fix: Use findByUsername instead of findByUserIdWithRole
        UserEntity user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new DisabledException("User is deactivated");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())  // Use username, not userId
                .password(user.getPassword())
                .roles(user.getRole().getRoleName())
                .build();
    }
}
