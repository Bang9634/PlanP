package com.drhong.service;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.drhong.model.User;

/**
 * JWT 토큰 생성 및 검증 서비스
 * 
 * @author bang9634
 * @since 2025-12-05
 */
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    /** JWT 서명 비밀키 (환경변수로 관리 권장) */
    private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY") != null 
        ? System.getenv("JWT_SECRET_KEY") 
        : "planp-secret-key-change-in-production";
    
    /** JWT 알고리즘 (HMAC256) */
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    
    /** JWT 발급자 */
    private static final String ISSUER = "planp-backend";
    
    /** 액세스 토큰 유효시간 (60분) */
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;
    
    /** 리프레시 토큰 유효시간 (7일) */
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;

    /**
     * 액세스 토큰 생성
     * 
     * @param user 사용자 정보
     * @return JWT 액세스 토큰
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
        
        String token = JWT.create()
            .withIssuer(ISSUER)
            .withSubject(user.getUserId())  // 사용자 ID
            .withClaim("name", user.getName())  // 이름
            .withClaim("email", user.getEmail())  // 이메일
            .withIssuedAt(now)  // 발급 시간
            .withExpiresAt(expiresAt)  // 만료 시간
            .sign(algorithm);
        
        logger.debug("액세스 토큰 생성: userId={}, expiresAt={}", user.getUserId(), expiresAt);
        return token;
    }

    /**
     * 리프레시 토큰 생성
     * 
     * @param userId 사용자 ID
     * @return JWT 리프레시 토큰
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);
        
        String token = JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm);
        
        logger.debug("리프레시 토큰 생성: userId={}, expiresAt={}", userId, expiresAt);
        return token;
    }

    /**
     * 토큰 검증 및 사용자 정보 추출
     * 
     * @param token JWT 토큰
     * @return 검증 성공 시 사용자 정보, 실패 시 빈 Optional
     */
    public Optional<User> validateToken(String token) {
        try {
            // JWT 검증기 생성
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
            
            // 토큰 검증 (서명 + 만료시간 자동 확인)
            DecodedJWT jwt = verifier.verify(token);
            
            // 토큰에서 사용자 정보 추출
            String userId = jwt.getSubject();
            String name = jwt.getClaim("name").asString();
            String email = jwt.getClaim("email").asString();
            
            // User 객체 생성 (DB 조회 없음!)
            User user = new User(userId, null, name, email);
            
            logger.debug("토큰 검증 성공: userId={}", userId);
            return Optional.of(user);
            
        } catch (JWTVerificationException e) {
            logger.warn("토큰 검증 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 토큰에서 사용자 ID 추출 (검증 없이)
     * 
     * @param token JWT 토큰
     * @return 사용자 ID 또는 null
     */
    public String getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            logger.warn("토큰 디코딩 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 토큰 만료 시간 확인
     * 
     * @param token JWT 토큰
     * @return 만료 시간 (epoch milliseconds) 또는 -1
     */
    public long getExpirationTime(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt().getTime();
        } catch (Exception e) {
            logger.warn("토큰 만료시간 확인 실패: {}", e.getMessage());
            return -1;
        }
    }
}