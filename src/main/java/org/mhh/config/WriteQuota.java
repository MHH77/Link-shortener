package org.mhh.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.HashMap;
import java.util.Map;

@Component
public final class WriteQuota implements HandlerInterceptor {
    private static final int MAX_CLIENTS = 10000;
    private final Map<String, Integer> counts = new HashMap<>();
    private final int limit;
    private long window = -1;

    public WriteQuota(@Value("${app.writes-per-minute:30}") int limit) {
        if (limit < 1) throw new IllegalArgumentException("Write quota must be positive");
        this.limit = limit;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equals(request.getMethod())) return true;
        long wait = retryAfter(request.getRemoteAddr(), System.currentTimeMillis() / 1000);
        if (wait == 0) return true;
        response.setStatus(429);
        response.setHeader("Retry-After", Long.toString(wait));
        return false;
    }

    synchronized long retryAfter(String address, long second) {
        long minute = second / 60;
        if (minute != window) {
            counts.clear();
            window = minute;
        }
        int count = counts.getOrDefault(address, 0);
        if (count >= limit || (count == 0 && counts.size() >= MAX_CLIENTS)) return 60 - second % 60;
        counts.put(address, count + 1);
        return 0;
    }
}
