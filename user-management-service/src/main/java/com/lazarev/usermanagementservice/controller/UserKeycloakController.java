package com.lazarev.usermanagementservice.controller;

import com.lazarev.model.dto.usermanagement.RoleMappingRequest;
import com.lazarev.model.dto.usermanagement.UserDto;
import com.lazarev.usermanagementservice.client.UsersKeycloakClient;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserKeycloakController {
    private final UsersKeycloakClient usersKeycloakClient;

    @GetMapping
    public ResponseEntity<List<UserRepresentation>> getAllUsers(){
        ResponseEntity<List<UserRepresentation>> usersResponse = usersKeycloakClient.getAllUsers();
        return ResponseEntity
                .status(usersResponse.getStatusCode())
                .body(usersResponse.getBody());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserRepresentation> getUserByUsername(@PathVariable String username){
        ResponseEntity<List<UserRepresentation>> usersResponse = usersKeycloakClient.getUserByUsername(username);
        List<UserRepresentation> users = usersResponse.getBody();
        if(users.size() == 1){
            return ResponseEntity.ok(users.get(0));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getUsersCount(){
        return usersKeycloakClient.getUsersCount();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto){
        return usersKeycloakClient.createUser(userDto);
    }

    @PostMapping("/{userId}/role-mappings/clients/{clientId}")
    ResponseEntity<?> addRolesToUser(@PathVariable String userId,
                                    @PathVariable String clientId,
                                    @RequestBody List<RoleMappingRequest> roles){
        return usersKeycloakClient.addRolesToUser(userId, clientId, roles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id){
        return usersKeycloakClient.deleteUserById(id);
    }

    @DeleteMapping("/{userId}/role-mappings/clients/{clientId}")
    ResponseEntity<?> deleteRolesFromUser(@PathVariable String userId,
                                     @PathVariable String clientId,
                                     @RequestBody List<RoleMappingRequest> roles){
        return usersKeycloakClient.deleteRoleFromUser(userId, clientId, roles);
    }
}
