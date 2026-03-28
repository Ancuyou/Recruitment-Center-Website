package com.example.tuyendung.util;

import org.springframework.stereotype.Component;

/**
 * SystemTimeProvider - Production implementation of TimeProvider
 * Uses System.currentTimeMillis() as the source of truth
 * 
 * SOLID - DIP: Injected into services instead of direct System.currentTimeMillis() calls
 */
@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
