package com.lazarev.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class Oauth2SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable()
                .authorizeExchange()
                    .pathMatchers("/eureka/**").permitAll()
                    .anyExchange()
                    .authenticated()
                    .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(reactiveJwtAuthenticationConverterAdapter());
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter reactiveJwtAuthenticationConverterAdapter() {
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter());
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtConverter());
        return jwtAuthenticationConverter;
    }

    @SuppressWarnings({"unchecked"})
    private Converter<Jwt, Collection<GrantedAuthority>> jwtConverter() {
        return jwt -> {
            Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
            Map<String, List<String>> roles = (Map<String, List<String>>) resourceAccess.get("order-processing-system-client");

            List<String> authorities = new ArrayList<>();
            if (roles != null) {
                authorities.addAll(roles.values().stream().flatMap(Collection::stream).toList());
            }

            return authorities.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .toList();
        };
    }
}
