package com.in.Blog_app.security.ratelimit;

import com.in.Blog_app.exception.RateLimitExceededException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    /**
     * Simple in-memory sliding-window limiter.
     * Suitable for a single app instance and easy to swap for Redis later.
     */
    public void checkLimit(String key, int maxRequests, Duration window, String message) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - window.toMillis();
        Deque<Long> bucket = buckets.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (bucket) {
            while (!bucket.isEmpty() && bucket.peekFirst() < windowStart) {
                bucket.pollFirst();
            }

            if (bucket.size() >= maxRequests) {
                long retryAfterMillis = Math.max(1000L, window.toMillis() - (now - bucket.peekFirst()));
                throw new RateLimitExceededException(message, Math.max(1L, (long) Math.ceil(retryAfterMillis / 1000.0)));
            }

            bucket.addLast(now);
        }
    }

    public void clear() {
        buckets.clear();
    }
}
