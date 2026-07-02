package com.smartcampus.erp.security;

public interface TokenBlacklistService {
    void blacklistToken(String token, long expirationTimeMs);
    boolean isBlacklisted(String token);
}
