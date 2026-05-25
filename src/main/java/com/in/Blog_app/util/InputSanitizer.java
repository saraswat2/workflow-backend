package com.in.Blog_app.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InputSanitizer {

    private static final Pattern DISALLOWED_CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");
    private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");

    public String sanitizeUsername(String value) {
        String sanitized = sanitizeIdentifier(value, 50, "username");
        if (!sanitized.matches("^[A-Za-z0-9._-]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username may contain only letters, numbers, dots, underscores, and hyphens");
        }
        return sanitized;
    }

    public String sanitizeEmail(String value) {
        String sanitized = sanitizeIdentifier(value, 100, "email").toLowerCase(Locale.ROOT);
        if (sanitized.contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must not contain spaces");
        }
        return sanitized;
    }

    public String sanitizeLoginIdentifier(String value) {
        String sanitized = sanitizeIdentifier(value, 100, "usernameOrEmail");
        return sanitized.contains("@") ? sanitizeEmail(sanitized) : sanitized;
    }

    public String sanitizeIdentifier(String value, int maxLength, String fieldName) {
        String sanitized = normalize(value, false);
        if (sanitized.contains("<") || sanitized.contains(">")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " contains invalid characters");
        }
        validateRequired(fieldName, sanitized);
        validateLength(sanitized, maxLength, fieldName);
        return sanitized;
    }

    public String sanitizeSingleLineText(String value, int maxLength, String fieldName) {
        String sanitized = normalize(value, false);
        validateRequired(fieldName, sanitized);
        validateLength(sanitized, maxLength, fieldName);
        return sanitized;
    }

    public String sanitizeMultilineText(String value, int maxLength, String fieldName) {
        String sanitized = normalize(value, true);
        validateRequired(fieldName, sanitized);
        validateLength(sanitized, maxLength, fieldName);
        return sanitized;
    }

    public String validatePassword(String value) {
        String candidate = value == null ? "" : value;
        if (!candidate.equals(candidate.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must not start or end with whitespace");
        }
        if (candidate.chars().anyMatch(Character::isISOControl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password contains invalid control characters");
        }
        return candidate;
    }

    private String normalize(String value, boolean allowNewLines) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC);
        normalized = DISALLOWED_CONTROL_CHARS.matcher(normalized).replaceAll("");

        if (!allowNewLines) {
            normalized = MULTI_WHITESPACE.matcher(normalized).replaceAll(" ");
        }

        return normalized.trim();
    }

    private void validateRequired(String fieldName, String value) {
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
    }

    private void validateLength(String value, int maxLength, String fieldName) {
        if (value.length() > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " exceeds max length of " + maxLength);
        }
    }
}
