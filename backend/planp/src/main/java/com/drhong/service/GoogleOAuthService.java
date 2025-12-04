package com.drhong.service;

import com.drhong.dto.GoogleUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Google OAuth 2.0 인증을 담당하는 서비스 클래스
 * 
 * <p>
 * Google Access Token의 유효성 검증과 사용자 정보 조회를 처리한다.
 * 이 서비스는 다른 서비스와의 순환 의존성을 피하기 위해 독립적으로 동작한다.
 * </p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>Google Access Token 검증</li>
 *   <li>Google 사용자 정보 조회 (이메일, 이름, Google ID)</li>
 *   <li>토큰 검증과 사용자 정보 조회 통합 처리</li>
 * </ul>
 * 
 * <h3>API 최적화</h3>
 * <p>
 * Google userinfo API는 유효하지 않은 토큰에 대해 401/403 오류를 반환하므로,
 * 별도의 tokeninfo API 호출 없이 userinfo 호출만으로 토큰 검증과 사용자 정보 조회를 
 * 동시에 처리할 수 있다.
 * </p>
 * 
 * @author wnwoghd
 * @since 2025-12-04
 */
public class GoogleOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthService.class);
    
    /** Google UserInfo API URL */
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * GoogleOAuthService 생성자
     */
    public GoogleOAuthService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
        
        logger.info("Google OAuth Service 초기화 완료");
    }
    
    /**
     * Google Access Token을 검증하고 사용자 정보를 반환한다.
     * 
     * <p>
     * 최적화: Google userinfo API는 토큰이 유효하지 않으면 오류를 반환하므로
     * 별도의 tokeninfo API 호출 없이 userinfo 호출만으로 검증과 정보 조회를 동시에 처리한다.
     * </p>
     * 
     * <h3>최적화된 처리 과정</h3>
     * <ol>
     *   <li>Access Token 유효성 검사 (null/empty 체크)</li>
     *   <li>Google userinfo API 직접 호출 (토큰 검증 + 사용자 정보 조회)</li>
     *   <li>성공 시 사용자 정보 DTO 반환, 실패 시 null</li>
     * </ol>
     * 
     * @param accessToken Google에서 발급받은 Access Token
     * @return 검증된 사용자 정보, 실패 시 null
     * 
     * @throws IllegalArgumentException accessToken이 null이거나 빈 문자열인 경우
     * 
     * @see #getUserInfo(String)
     */
    public GoogleUser verifyToken(String accessToken) {
        logger.info("Google Access Token 검증 시작");
        
        if (accessToken == null || accessToken.trim().isEmpty()) {
            logger.warn("Access Token이 null이거나 빈 문자열입니다");
            throw new IllegalArgumentException("Access Token은 null이거나 빈 문자열일 수 없습니다");
        }
        
        try {
            // 최적화: userinfo API 직접 호출로 토큰 검증과 사용자 정보를 한 번에 처리
            // Google userinfo API는 잘못된 토큰에 대해 401/403 오류를 반환하므로
            // 별도의 tokeninfo API 검증이 불필요함
            GoogleUser user = getUserInfo(accessToken);
            
            if (user != null) {
                logger.info("Google 토큰 검증 및 사용자 정보 조회 성공: email={}", user.getEmail());
            } else {
                logger.warn("Google 토큰 검증 실패 또는 사용자 정보 없음");
            }
            
            return user;
            
        } catch (Exception e) {
            logger.error("Google 토큰 검증 중 예외 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Google User Info API로 사용자 정보를 조회한다.
     * 
     * <p>
     * 이 메서드는 Google의 userinfo API를 호출하여 사용자 정보를 가져온다.
     * 동시에 토큰 검증도 수행되므로 별도의 tokeninfo 호출이 불필요하다.
     * </p>
     * 
     * @param accessToken Google Access Token
     * @return 사용자 정보 객체, 실패 시 null
     */
    private GoogleUser getUserInfo(String accessToken) {
        try {
            // Google User Info API 호출 (필요한 필드만 요청)
            String url = GOOGLE_USERINFO_URL + 
                         "?access_token=" + accessToken +
                         "&fields=id,email,name";  // 필요한 필드만 지정
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 응답 상태 확인 (토큰 검증 포함)
            if (response.statusCode() != 200) {
                logger.warn("Google User Info API 호출 실패: HTTP {}, 응답={}", response.statusCode(), response.body());
                return null;
            }

            // JSON 응답 파싱
            return parseUserInfoResponse(response.body());

        } catch (URISyntaxException e) {
            logger.error("Google User Info API URL 오류: {}", e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error("Google User Info API 통신 오류: {}", e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Google User Info API 호출 중단됨: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Google API 응답 JSON을 GoogleUser 객체로 변환한다.
     * 
     * @param jsonResponse Google API JSON 응답
     * @return GoogleUser 객체, 파싱 실패 시 null
     */
    private GoogleUser parseUserInfoResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            
            if (jsonObject == null) {
                logger.error("Google API 응답이 null입니다");
                return null;
            }

            // 필수 필드 검증
            if (!jsonObject.has("id") || !jsonObject.has("email")) {
                logger.error("Google API 응답에서 필수 필드 누락: {}", jsonResponse);
                return null;
            }

            // GoogleUser 객체 생성
            GoogleUser user = new GoogleUser();
            user.setId(jsonObject.get("id").getAsString());
            user.setEmail(jsonObject.get("email").getAsString());
            
            // 선택적 필드 (이름)
            if (jsonObject.has("name") && !jsonObject.get("name").isJsonNull()) {
                user.setName(jsonObject.get("name").getAsString());
            }

            logger.debug("Google 사용자 정보 변환 성공: 이메일={}, 이름={}", user.getEmail(), user.getName());
            return user;

        } catch (JsonSyntaxException e) {
            logger.error("Google API 응답 JSON 파싱 실패: Response={}", jsonResponse, e);
            return null;
        } catch (Exception e) {
            logger.error("Google 사용자 정보 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
            return null;
        }
    }
}