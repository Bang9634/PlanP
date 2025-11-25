package com.drhong.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.GoogleOAuthConfig;
import com.drhong.dto.GoogleUserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Google OAuth 인증을 처리하는 서비스 클래스
 * <p>
 * Google에서 발급받은 인증 코드를 통해 액세스 토큰을 받고,
 * 해당 토큰으로 사용자 정보를 가져오는 기능을 제공한다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>인증 코드를 액세스 토큰으로 교환</li>
 *   <li>액세스 토큰으로 Google 사용자 정보 조회</li>
 *   <li>소셜 로그인 프로세스 통합 처리</li>
 * </ul>
 * 
 * @author wnwoghd
 * @since 2025-11-25
 */
public class GoogleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);
    
    private final HttpClient httpClient;
    private final Gson gson;
    
    /**
     * 기본 생성자
     * <p>
     * HTTP 클라이언트와 JSON 파서를 초기화한다.
     * </p>
     */
    public GoogleAuthService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
        logger.info("GoogleAuthService 초기화 완료");
    }

    /**
     * 인증 코드를 액세스 토큰으로 교환
     * <p>
     * Google OAuth 인증 과정에서 받은 인증 코드를 
     * 액세스 토큰으로 교환한다.
     * </p>
     * 
     * @param authCode Google에서 받은 인증 코드
     * @return 액세스 토큰 문자열, 실패 시 null
     */
    public String exchangeCodeForToken(String authCode) {
        logger.debug("인증 코드를 액세스 토큰으로 교환 시작");

        try {
            // 요청 파라미터 구성
            Map<String, String> params = new HashMap<>();
            params.put("code", authCode);
            params.put("client_id", GoogleOAuthConfig.getClientId());
            params.put("client_secret", GoogleOAuthConfig.getClientSecret());
            params.put("redirect_uri", GoogleOAuthConfig.getRedirectUri());
            params.put("grant_type", "authorization_code");

            // POST 요청 본문 생성
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() > 0) {
                    postData.append('&');
                }
                postData.append(param.getKey()).append('=').append(param.getValue());
            }

            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GoogleOAuthConfig.getTokenUrl()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData.toString()))
                .build();

            // 요청 전송
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                String accessToken = jsonResponse.get("access_token").getAsString();
                
                logger.info("액세스 토큰 획득 성공");
                return accessToken;
            } else {
                logger.error("토큰 교환 실패: HTTP {}, 응답: {}", 
                    response.statusCode(), response.body());
                return null;
            }

        } catch (Exception e) {
            logger.error("토큰 교환 중 예외 발생", e);
            return null;
        }
    }

    /**
     * 액세스 토큰으로 Google 사용자 정보 조회
     * <p>
     * 유효한 액세스 토큰을 사용하여 Google 사용자 정보를 가져온다.
     * </p>
     * 
     * @param accessToken Google 액세스 토큰
     * @return GoogleUserInfo 객체, 실패 시 null
     */
    public GoogleUserInfo getUserInfo(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            logger.warn("액세스 토큰이 비어있습니다");
            return null;
        }

        logger.debug("Google 사용자 정보 조회 시작");

        try {
            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GoogleOAuthConfig.getUserinfoUrl()))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

            // 요청 전송
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject userJson = gson.fromJson(response.body(), JsonObject.class);
                
                GoogleUserInfo userInfo = new GoogleUserInfo();
                userInfo.setId(userJson.get("id").getAsString());
                userInfo.setEmail(userJson.get("email").getAsString());
                userInfo.setName(userJson.get("name").getAsString());
                userInfo.setVerifiedEmail(userJson.get("verified_email").getAsBoolean());
                
                logger.info("Google 사용자 정보 조회 성공: email={}", userInfo.getEmail());
                return userInfo;
            } else {
                logger.error("사용자 정보 조회 실패: HTTP {}, 응답: {}", 
                    response.statusCode(), response.body());
                return null;
            }

        } catch (Exception e) {
            logger.error("사용자 정보 조회 중 예외 발생", e);
            return null;
        }
    }

    /**
     * 소셜 로그인 통합 처리
     * <p>
     * 인증 코드부터 사용자 정보 조회까지 전체 프로세스를 처리한다.
     * </p>
     * 
     * @param authCode Google 인증 코드
     * @return GoogleUserInfo 객체, 실패 시 null
     */
    public GoogleUserInfo processGoogleLogin(String authCode) {
        logger.info("Google 소셜 로그인 처리 시작");

        // 1. 인증 코드를 액세스 토큰으로 교환
        String accessToken = exchangeCodeForToken(authCode);
        if (accessToken == null) {
            logger.error("액세스 토큰 획득 실패");
            return null;
        }

        // 2. 액세스 토큰으로 사용자 정보 조회
        GoogleUserInfo userInfo = getUserInfo(accessToken);
        if (userInfo == null) {
            logger.error("사용자 정보 조회 실패");
            return null;
        }

        logger.info("Google 소셜 로그인 처리 완료: email={}", userInfo.getEmail());
        return userInfo;
    }

    /**
     * 설정 유효성 검증
     * <p>
     * Google OAuth 설정이 올바르게 구성되어 있는지 확인한다.
     * </p>
     * 
     * @return 설정이 유효하면 true
     */
    public boolean validateConfiguration() {
        try {
            GoogleOAuthConfig.getClientId();
            GoogleOAuthConfig.getClientSecret();
            logger.debug("Google OAuth 설정 유효성 검증 통과");
            return true;
        } catch (Exception e) {
            logger.error("Google OAuth 설정 유효성 검증 실패", e);
            return false;
        }
    }
}