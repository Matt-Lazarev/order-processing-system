package com.lazarev.discoveryserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Value("${application.eureka.username}")
    private String eurekaUserUsername;

    @Value("${application.eureka.password}")
    private String eurekaUserPassword;

    @Value("${application.eureka.role}")
    private String eurekaUserRole;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests()
                    .anyRequest()
                    .authenticated()
                    .and()
                .httpBasic();
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        InMemoryUserDetailsManager uds = new InMemoryUserDetailsManager();
        UserDetails user = User
                .withUsername(eurekaUserUsername)
                .password(passwordEncoder().encode(eurekaUserPassword))
                .roles(eurekaUserRole)
                .build();
        uds.createUser(user);
        return uds;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
