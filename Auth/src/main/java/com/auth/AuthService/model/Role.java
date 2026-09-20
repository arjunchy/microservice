package com.auth.AuthService.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    USER,
    ADMIN;

    @JsonCreator
    public static Role from(String value) {
        return value == null ? null : Role.valueOf(value.toUpperCase());
    }

}