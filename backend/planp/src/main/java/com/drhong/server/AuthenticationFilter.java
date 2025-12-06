package com.drhong.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.model.User;
import com.drhong.service.JwtService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * JWT 토큰 인증 필터
 * 
 * <p>
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고,
 * 공개 엔드포인트는 검증을 생략한다.
 * 인증된 사용자 정보를 HttpExchange에 저장하여 핸들러에서 사용할 수 있게 한다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>Authorization 헤더에서 Bearer 토큰 추출</li>
 *   <li>공개 엔드포인트 검증 생략</li>
 *   <li>토큰 유효성 검증 (TokenService)</li>
 *   <li>인증된 사용자 정보 HttpExchange에 저장</li>
 * </ul>
 * 
 * <h3>공개 엔드포인트 (토큰 불필요):</h3>
 * <ul>
 *   <li>GET /health</li>
 *   <li>POST /api/users/signup</li>
 *   <li>POST /api/users/login</li>
 * </ul>
 * 
 * <h3>보호된 엔드포인트 (토큰 필요):</h3>
 * <ul>
 *   <li>GET /api/users/{userId}</li>
 *   <li>PUT /api/users/{userId}</li>
 *   <li>DELETE /api/users/{userId}</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-12-03
 * 
 * @see TokenService
 * @see com.drhong.model.AuthToken
 */
public class AuthenticationFilter extends Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    /**
     * 인증이 필요 없는 공개 엔드포인트 목록
     * 
     * <p>
     * "HTTP메서드:경로" 형식으로 저장된다.
     * 대소문자를 구분하며, 정확히 일치해야 한다.
     * </p>
     */
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "GET:/health",
        "POST:/api/users/signup",
        "POST:/api/users/auth/google"
    );

    private final JwtService jwtService;

    /**
     * AuthenticationFilter 생성자
     * 
     * @param tokenService 토큰 검증 서비스
     * 
     * @throws NullPointerException tokenService가 null인 경우
     */
    public AuthenticationFilter(JwtService jwtService) {
        if (jwtService == null) {
            throw new NullPointerException("JwtService는 null일 수 없습니다");
        }
        this.jwtService = jwtService;
        logger.info("AuthenticationFilter 초기화 완료");
    }

    /**
     * HTTP 요청 필터링 메인 메서드
     * 
     * <p>
     * 요청 경로와 메서드를 확인하여 인증이 필요한지 판단한다.
     * 공개 엔드포인트는 토큰 검증을 생략하고, 보호된 엔드포인트는 토큰을 검증한다.
     * </p>
     * @param exchange HTTP 요청/응답 교환 객체
     * @param chain 다음 필터나 핸들러로 요청을 전달하는 체인
     * @throws IOException HTTP 처리 중 I/O 오류가 발생한 경우
     */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String endpoint = method + ":" + path;
        
        logger.debug("인증 필터 시작: {} {}", method, path);
        
        // 공개 엔드포인트 확인
        if (isPublicEndpoint(endpoint)) {
            logger.debug("공개 엔드포인트 - 토큰 검증 생략: {}", endpoint);
            chain.doFilter(exchange);
            return;
        }
        
        // Authorization 헤더에서 토큰 추출
        String token = extractBearerToken(exchange);
        
        if (token == null) {
            logger.warn("토큰 없음: {}", endpoint);
            sendErrorResponse(exchange, 401, "인증이 필요합니다");
            return;
        }
        
        // 토큰 검증
        try {
            Optional<User> user = jwtService.validateToken(token);
            
            if (user.isEmpty()) {
                logger.warn("유효하지 않은 토큰: {}", endpoint);
                sendErrorResponse(exchange, 401, "유효하지 않은 토큰입니다");
                return;
            }
            
            // 사용자 정보 저장 (핸들러에서 사용)
            exchange.setAttribute("userId", user.get().getUserId());
            exchange.setAttribute("user", user.get());
            
            logger.debug("인증 성공: userId={}, endpoint={}", user.get().getUserId(), endpoint);
            
            // 다음 필터/핸들러로 전달
            chain.doFilter(exchange);
            
        } catch (Exception e) {
            logger.error("토큰 검증 중 오류 발생", e);
            sendErrorResponse(exchange, 401, "토큰 검증 실패");
        }
    }

    /**
     * 공개 엔드포인트 확인
     * 
     * <p>
     * "메서드:경로" 형식으로 공개 엔드포인트 목록과 비교한다.
     * 정확히 일치하는 경우에만 true를 반환한다.
     * </p>
     * 
     * @param endpoint "GET:/health" 형식의 엔드포인트
     * @return 공개 엔드포인트이면 true, 아니면 false
     */
    private boolean isPublicEndpoint(String endpoint) {
        return PUBLIC_ENDPOINTS.stream()
            .anyMatch(endpoint::equals);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * 
     * <p>
     * "Bearer {token}" 형식에서 토큰 부분만 추출한다.
     * 형식이 올바르지 않으면 null을 반환한다.
     * </p>
     * 
     * @param exchange HTTP 교환 객체
     * @return 추출된 토큰 또는 null
     */
    private String extractBearerToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // "Bearer " 이후 문자열
        }
        
        return null;
    }

    /**
     * 인증 실패 응답 전송
     * 
     * @param exchange HTTP 교환 객체
     * @param statusCode HTTP 상태 코드 (401)
     * @param message 오류 메시지
     * @throws IOException 응답 전송 실패 시
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String jsonResponse = String.format(
            "{\"success\":false,\"message\":\"%s\",\"timestamp\":%d}",
            message,
            System.currentTimeMillis()
        );
        
        byte[] responseBytes = jsonResponse.getBytes("UTF-8");
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (var os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.debug("인증 오류 응답 전송: statusCode={}, message={}", statusCode, message);
    }

    /**
     * 필터 설명 반환
     */
    @Override
    public String description() {
        return "JWT Authentication Filter - 토큰 기반 사용자 인증";
    }
}