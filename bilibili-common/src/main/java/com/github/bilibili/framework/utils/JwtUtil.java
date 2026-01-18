/*
 * MIT License
 *
 * Copyright (c) [2025] [OrcasVik]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *<link>https://github.com/OrcsaVik</link>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.github.bilibili.framework.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class JwtUtil {

    // FR-001: Access Token 7 days, Refresh Token 14 days
    public static final long ACCESS_TOKEN_EXPIRATION = 7L * 24 * 60 * 60 * 1000;
    public static final long REFRESH_TOKEN_EXPIRATION = 14L * 24 * 60 * 60 * 1000;

    // CLAIM KEYS
    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_TYPE = "typ"; // "acc" or "ref"
    private static final String CLAIM_FINGERPRINT = "fgp"; // Device Fingerprint Hash

    // FIXED: Use a stable key. In Prod, load this from @Value("${jwt.secret}")
    // This base64 string is just a placeholder for "LinusTorvaldsIsWatchingYourCodeQuality2025"
    private static final String SECRET_STRING = "TGludXNUb3J2YWxkc0lzV2F0Y2hpbmdZb3VyQ29kZVF1YWxpdHkyMDI1";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET_STRING));

    /**
     * FR-002: Generate Device Fingerprint Hash
     * Combines immutable device properties into a hash.
     */
    public static String calculateFingerprint(String deviceId, String userAgent) {
        if (deviceId == null)
            deviceId = "unknown_device";
        if (userAgent == null)
            userAgent = "unknown_ua";

        // Simple MD5 or SHA-256 hash to keep token size small
        String raw = deviceId + "::" + userAgent;
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * FR-001: Generate Dual Tokens
     */
    public static TokenPair generateDualTokens(UserContextInfo user, String deviceFingerprint) {
        String accessToken = createToken(user.getUserId(), deviceFingerprint, "acc", ACCESS_TOKEN_EXPIRATION);
        String refreshToken = createToken(user.getUserId(), deviceFingerprint, "ref", REFRESH_TOKEN_EXPIRATION);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpire(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION)
                .refreshExpire(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION)
                .build();
    }

    private static String createToken(String userId, String fingerprint, String type, long duration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_TYPE, type);
        claims.put(CLAIM_FINGERPRINT, fingerprint);

        return Jwts.builder()
                .setSubject(userId)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + duration))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate Token & Verify Fingerprint (FR-002)
     * @param token The JWT
     * @param currentFingerprint The calculated fingerprint from current request headers
     * @param expectedType "acc" or "ref"
     */
    public static Claims validateToken(String token, String currentFingerprint, String expectedType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 1. Check Token Type (Prevent using Refresh Token to access API)
            String type = claims.get(CLAIM_TYPE, String.class);
            if (!expectedType.equals(type)) {
                throw new IllegalArgumentException("Invalid token type. Expected: " + expectedType);
            }

            // 2. FR-002 & FR-003: Check Device Fingerprint
            String tokenFingerprint = claims.get(CLAIM_FINGERPRINT, String.class);
            if (tokenFingerprint == null || !tokenFingerprint.equals(currentFingerprint)) {
                throw new SecurityException("Device Fingerprint Mismatch! Force Logout.");
            }

            return claims;
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Token has expired", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token validation failed: " + e.getMessage(), e);
        }
    }

    public static UserContextInfo extractUserContextInfo(Claims claims) {
        UserContextInfo info = new UserContextInfo();
        info.setUserId(claims.get(CLAIM_USER_ID, String.class));
        return info;
    }

    public TokenPair refreshToken(String refreshToken, String deviceId, String userAgent) {
        // 1. Calculate Fingerprint
        String currentFingerprint = JwtUtil.calculateFingerprint(deviceId, userAgent);

        // 2. Validate Refresh Token (Must match device!)
        Claims claims = JwtUtil.validateToken(refreshToken, currentFingerprint, "ref");
        String userId = claims.getSubject();

        // 3. (Optional) Check Redis if this specific Refresh Token was already used (Reuse Detection)
        // if (redis.isUsed(refreshToken)) -> revoke all tokens for user (Security Risk)

        // 4. Generate NEW Pair
        UserContextInfo user = new UserContextInfo(userId);
        TokenPair newTokens = JwtUtil.generateDualTokens(user, currentFingerprint);

        // 5. Revoke Old Token (Optional - JWTs can't be revoked without Redis blacklist)
        // redis.addToBlacklist(refreshToken, remainingTime);

        return newTokens;
    }
    // --- DTOs ---

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserContextInfo {

        private String userId;
        // Add roles/permissions here if needed
    }

    @Data
    @Builder
    public static class TokenPair {

        private String accessToken;
        private String refreshToken;
        private long accessExpire;
        private long refreshExpire;
    }
}