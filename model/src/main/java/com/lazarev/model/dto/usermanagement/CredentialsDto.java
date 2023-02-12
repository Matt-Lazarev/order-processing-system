package com.lazarev.model.dto.usermanagement;

public record CredentialsDto (String type,
                              String value,
                              Boolean temporary) { }
