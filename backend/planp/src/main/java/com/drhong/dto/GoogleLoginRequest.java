package com.drhong.dto;

/**
 * 구글 로그인 요청 데이터를 담는 DTO 클래스
 * 
 * <p>
 * 프론트엔드에서 구글 OAuth로 받은 액세스 토큰을 
 * 백엔드로 전달할 때 사용하는 데이터 전송 객체이다.
 * </p>
 * 
 * @author wnwoghd
 * @since 2025-12-03
 */
public class GoogleLoginRequest {
    private String accessToken;
    
    /**
     * 기본 생성자
     */
    public GoogleLoginRequest() {}
    
    /**
     * 액세스 토큰을 포함한 생성자
     * 
     * @param accessToken 구글에서 받은 액세스 토큰
     */
    public GoogleLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }
    
    /**
     * 액세스 토큰을 반환한다.
     * 
     * @return Google OAuth 액세스 토큰
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * 액세스 토큰을 설정한다.
     * 
     * @param accessToken Google OAuth 액세스 토큰
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    @Override
    public String toString() {
        return String.format("GoogleLoginRequest{accessToken='%s'}", 
            accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : null);
    }
}