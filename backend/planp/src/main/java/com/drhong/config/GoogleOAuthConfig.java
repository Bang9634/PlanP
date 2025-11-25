package com.drhong.config;

/**
 * Google OAuth 2.0 설정을 관리하는 클래스
 * <p>
 * Google Cloud Console에서 발급받은 클라이언트 정보를 관리하고,
 * OAuth 인증에 필요한 설정값들을 제공한다.
 * </p>
 * 
 * <h3>설정 방법:</h3>
 * <ol>
 *   <li>Google Cloud Console에서 프로젝트 생성</li>
 *   <li>OAuth 2.0 클라이언트 ID 생성 (웹 애플리케이션 타입)</li>
 *   <li>승인된 리디렉션 URI 설정</li>
 *   <li>클라이언트 ID와 Secret을 환경변수 또는 설정 파일에 저장</li>
 * </ol>
 * 
 * @author wnwoghd
 * @since 2025-11-25
 */
public class GoogleOAuthConfig {
    
    /** Google OAuth 클라이언트 ID - 환경변수에서 가져오기 */
    private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    
    /** Google OAuth 클라이언트 시크릿 - 환경변수에서 가져오기 */
    private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
    
    /** Google OAuth 리디렉션 URI */
    private static final String REDIRECT_URI = System.getenv("GOOGLE_REDIRECT_URI");
    
    /** Google OAuth 스코프 - 기본 프로필과 이메일 정보 */
    private static final String SCOPE = "openid email profile";
    /*
    * 사용자가 "구글 로그인" 버튼 클릭
    * 우리 서버가 AUTH_URL + 파라미터로 리디렉션
    * 구글에서 로그인 후 인증 코드 반환
    * 우리 서버가 TOKEN_URL에 POST 요청으로 토큰 교환
    * USERINFO_URL에서 사용자 정보 가져오기
    */
    /** Google OAuth 인증 URL */
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    
    /** Google OAuth 토큰 URL */
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    
    /** Google 사용자 정보 API URL */
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    /**
     * Google OAuth 클라이언트 ID 반환
     * 
     * @return 클라이언트 ID 문자열
     * @throws IllegalStateException 환경변수가 설정되지 않은 경우
     */
    public static String getClientId() {
        if (CLIENT_ID == null || CLIENT_ID.trim().isEmpty()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID 환경변수가 설정되지 않았습니다.");
        }
        return CLIENT_ID;
    }

    /**
     * Google OAuth 클라이언트 시크릿 반환
     * 
     * @return 클라이언트 시크릿 문자열
     * @throws IllegalStateException 환경변수가 설정되지 않은 경우
     */
    public static String getClientSecret() {
        if (CLIENT_SECRET == null || CLIENT_SECRET.trim().isEmpty()) {
            throw new IllegalStateException("GOOGLE_CLIENT_SECRET 환경변수가 설정되지 않았습니다.");
        }
        return CLIENT_SECRET;
    }

    /**
     * Google OAuth 리디렉션 URI 반환
     * 
     * @return 리디렉션 URI 문자열
     */
    public static String getRedirectUri() {
        if (REDIRECT_URI == null || REDIRECT_URI.trim().isEmpty()) {
            int port = EnvironmentConfig.getPort();
            String host = EnvironmentConfig.getHost();
            return String.format("http://%s:%d/api/auth/google/callback", host, port);
        }
        return REDIRECT_URI;
    }

    // ...existing code...

    /**
     * Google OAuth 스코프 반환
     * 
     * @return 요청할 스코프 문자열
     */
    public static String getScope() {
        return SCOPE;
    }

    /**
     * Google OAuth 인증 URL 반환
     * 
     * @return 인증 URL 문자열
     */
    public static String getAuthUrl() {
        return AUTH_URL;
    }

    /**
     * Google OAuth 토큰 URL 반환
     * 
     * @return 토큰 URL 문자열
     */
    public static String getTokenUrl() {
        return TOKEN_URL;
    }

    /**
     * Google 사용자 정보 API URL 반환
     * 
     * @return 사용자 정보 API URL 문자열
     */
    public static String getUserinfoUrl() {
        return USERINFO_URL;
    }

    /**
     * 설정 유효성 검증
     * <p>
     * 필수 환경변수들이 모두 설정되어 있는지 확인한다.
     * </p>
     * 
     * @return 모든 설정이 유효하면 true
     */
    public static boolean isConfigurationValid() {
        try {
            getClientId();
            getClientSecret();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * 인증 URL 생성
     * <p>
     * 클라이언트를 Google 인증 페이지로 리디렉션하기 위한 완전한 URL을 생성한다.
     * </p>
     * @return 완성된 인증 URL
     */
    public static String buildAuthUrl() {
        StringBuilder url = new StringBuilder(AUTH_URL);
        url.append("?response_type=code");
        url.append("&client_id=").append(getClientId());
        url.append("&redirect_uri=").append(getRedirectUri());
        url.append("&scope=").append(SCOPE.replace(" ", "%20"));
        return url.toString();
    }
}