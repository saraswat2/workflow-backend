package com.in.Blog_app.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientRequestUtils {

    private ClientRequestUtils() {
    }

    public static String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
