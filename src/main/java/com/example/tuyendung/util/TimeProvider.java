package com.example.tuyendung.util;

/**
 * TimeProvider interface for abstraction of time source.
 * Enables dependency injection of time for testing and production use.
 * 
 * SOLID - DIP: Depend on this abstraction instead of System.currentTimeMillis()
 */
public interface TimeProvider {

    /**
     * Get current time in milliseconds since epoch
     * @return current time in milliseconds
     */
    long getCurrentTimeMillis();

    /**
     * Get current time in seconds since epoch
     * @return current time in seconds
     */
    long getCurrentTimeSeconds();
}
