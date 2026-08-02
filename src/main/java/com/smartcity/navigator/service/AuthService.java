package com.smartcity.navigator.service;

import java.util.Objects;

import com.smartcity.navigator.utils.AppLogger;

/**
 * Service responsible for managing user authentication.
 * Highly cohesive service class focused on validation.
 *
 * @author Smart City Route Navigator Team
 */
public class AuthService {

    private static final String USERNAME_ENVIRONMENT_VARIABLE = "SMART_CITY_USERNAME";
    private static final String PASSWORD_ENVIRONMENT_VARIABLE = "SMART_CITY_PASSWORD";
    private static final String DEFAULT_DEMO_USERNAME = "admin";
    private static final String DEFAULT_DEMO_PASSWORD = "admin";

    private final String expectedUsername;
    private final char[] expectedPassword;

    /**
     * Creates the local authentication gateway. Deployment credentials can be
     * supplied with the {@value #USERNAME_ENVIRONMENT_VARIABLE} and
     * {@value #PASSWORD_ENVIRONMENT_VARIABLE} environment variables. The
     * bundled {@code admin/admin} credentials are retained only for local demo
     * use, so the existing project workflow continues to work out of the box.
     */
    public AuthService() {
        this(resolveCredential(USERNAME_ENVIRONMENT_VARIABLE, DEFAULT_DEMO_USERNAME),
                resolveCredential(PASSWORD_ENVIRONMENT_VARIABLE, DEFAULT_DEMO_PASSWORD));
    }

    AuthService(String expectedUsername, String expectedPassword) {
        this.expectedUsername = requireCredential(expectedUsername, "Expected username");
        this.expectedPassword = requireCredential(expectedPassword, "Expected password").toCharArray();
    }

    /**
     * Authenticates user credentials without blocking the Event Dispatch
     * Thread. The caller owns the supplied password array and should erase it
     * after this method returns.
     */
    public boolean authenticate(String username, char[] password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            return false;
        }

        boolean isAuthenticated = expectedUsername.equalsIgnoreCase(username.trim())
                && passwordsMatch(password, expectedPassword);

        if (isAuthenticated) {
            AppLogger.info("Successful authentication gateway entry.");
        } else {
            AppLogger.warn("Unauthorized credential attempt.");
        }

        return isAuthenticated;
    }

    private static String resolveCredential(String environmentVariable, String fallback) {
        String configuredValue = System.getenv(environmentVariable);
        return configuredValue == null || configuredValue.isBlank() ? fallback : configuredValue;
    }

    private static String requireCredential(String value, String label) {
        String credential = Objects.requireNonNull(value, label + " must not be null").trim();
        if (credential.isEmpty()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return credential;
    }

    /**
     * Compares the complete candidate and expected password arrays before
     * returning, avoiding an early return that would disclose partial matches.
     */
    private static boolean passwordsMatch(char[] candidate, char[] expected) {
        int difference = candidate.length ^ expected.length;
        int longestLength = Math.max(candidate.length, expected.length);

        for (int index = 0; index < longestLength; index++) {
            char candidateCharacter = index < candidate.length ? candidate[index] : 0;
            char expectedCharacter = index < expected.length ? expected[index] : 0;
            difference |= candidateCharacter ^ expectedCharacter;
        }
        return difference == 0;
    }
}
