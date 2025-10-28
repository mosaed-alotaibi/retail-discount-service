package io.mosaed.retaildiscountservice.infrastructure.config;

/// @author MOSAED ALOTAIBI

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Lambda can be replaced with method reference (thanks to my IDE hint)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/bills/health").permitAll()
                        .requestMatchers("/actuator/health").permitAll() // Just for my sanity
                        // /bills/** matches /bills/calculate, /bills/123, /bills/foo/bar, etc.
                        .requestMatchers("/bills/**").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {})

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Use DelegatingPasswordEncoder to support {noop}, {bcrypt}, {pbkdf2}, etc.
        // This allows the {noop}password format in CustomerUserDetailsService

        // Sorry watchers... I was almost blinking, so I didn't notice noop doesn't wouldn't work with BCryptPasswordEncoder :)
        // Actually BCrypt doesn't understand noop
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
