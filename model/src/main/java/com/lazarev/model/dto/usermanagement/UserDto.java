package com.lazarev.model.dto.usermanagement;

import java.util.List;

public record UserDto (String username,
                       String firstName,
                       String lastName,
                       String email,
                       Boolean enabled,
                       List<CredentialsDto> credentials) { }
