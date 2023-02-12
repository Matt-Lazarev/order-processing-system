package com.lazarev.usermanagementservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class OAuth2FeignConfig {

    @Value("${spring.security.oauth2.client.registration.admin-cli.client-id}")
    public String clientId;

    @Bean
    public RequestInterceptor requestInterceptor(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientManager manager) {
        ClientRegistration client = clientRegistrationRepository.findByRegistrationId(clientId);
        return request ->
                request.header("Authorization", "Bearer " + getAccessToken(client, manager));
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            OAuth2AuthorizedClientService authorizedClientService,
            ClientRegistrationRepository clientRegistrationRepository) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    private String getAccessToken(
            ClientRegistration clientRegistration,
            OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistration.getRegistrationId())
                .principal(createPrincipal(clientRegistration))
                .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(oAuth2AuthorizeRequest);
        if (Objects.isNull(client)) {
            throw new IllegalStateException("Authorization failed for client " + clientRegistration.getClientId());
        }
        return client.getAccessToken().getTokenValue();
    }

    private Authentication createPrincipal(ClientRegistration clientRegistration) {
        return new Authentication() {
            @Override
            public String getName() {
                return clientRegistration.getClientId();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.emptySet();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return this;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };
    }
}
