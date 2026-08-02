package com.smartcity.navigator.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private final AuthService authService = new AuthService("operator", "route-safe-password");

    @Test
    void acceptsConfiguredCredentialsWithoutChangingTheCallerPasswordArray() {
        char[] password = "route-safe-password".toCharArray();

        assertTrue(authService.authenticate("OPERATOR", password));
        assertTrue(java.util.Arrays.equals("route-safe-password".toCharArray(), password));
    }

    @Test
    void rejectsIncorrectOrIncompleteCredentials() {
        assertFalse(authService.authenticate("operator", "wrong-password".toCharArray()));
        assertFalse(authService.authenticate("", "route-safe-password".toCharArray()));
        assertFalse(authService.authenticate(null, "route-safe-password".toCharArray()));
        assertFalse(authService.authenticate("operator", null));
    }
}
