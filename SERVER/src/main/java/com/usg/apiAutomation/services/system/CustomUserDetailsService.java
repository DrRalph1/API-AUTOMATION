package com.usg.apiAutomation.services.system;

import com.usg.apiAutomation.entities.UserEntity;
import com.usg.apiAutomation.repositories.AppUserRepository;
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

        UserEntity user = appUserRepository.findByUserIdWithRole(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new DisabledException("User is deactivated");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())       // <- same ID you use in login
                .password(user.getPassword())         // <- BCrypt password
                .roles(user.getRole().getRoleName())  // <- your DB role
                .build();
    }
}
