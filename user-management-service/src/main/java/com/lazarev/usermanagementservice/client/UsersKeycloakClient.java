package com.lazarev.usermanagementservice.client;

import com.lazarev.model.dto.usermanagement.RoleMappingRequest;
import com.lazarev.model.dto.usermanagement.UserDto;
import com.lazarev.usermanagementservice.config.OAuth2FeignConfig;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "users-keycloak-client",
        url = "http://localhost:8181/admin/realms/${application.security.oauth2.realm}",
        configuration = OAuth2FeignConfig.class)
public interface UsersKeycloakClient {

    @GetMapping("/users")
    ResponseEntity<List<UserRepresentation>> getAllUsers();

    @GetMapping("/users")
    ResponseEntity<List<UserRepresentation>> getUserByUsername(@RequestParam("username") String username);

    @GetMapping("/users/count")
    ResponseEntity<Integer> getUsersCount();

    @PostMapping("/users")
    ResponseEntity<?> createUser(@RequestBody UserDto userDto);

    @PostMapping("/users/{userId}/role-mappings/clients/{clientId}")
    ResponseEntity<?> addRolesToUser(@PathVariable("userId") String userId,
                                    @PathVariable("clientId") String clientId,
                                    @RequestBody List<RoleMappingRequest> roles);

    @DeleteMapping("/users/{id}")
    ResponseEntity<?> deleteUserById(@PathVariable("id") String id);

    @DeleteMapping("/users/{userId}/role-mappings/clients/{clientId}")
    ResponseEntity<?> deleteRoleFromUser(@PathVariable("userId") String userId,
                                     @PathVariable("clientId") String clientId,
                                     @RequestBody List<RoleMappingRequest> roles);
}
