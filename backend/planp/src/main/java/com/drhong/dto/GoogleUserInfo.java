package com.drhong.dto;

/**
 * Google OAuth로부터 받은 사용자 정보를 담는 DTO 클래스
 * <p>
 * Google People API 또는 OAuth 2.0 userinfo endpoint에서 
 * 받은 사용자 정보를 Java 객체로 매핑한다.
 * </p>
 * 
 * @author wnwoghd
 * @since 2025-11-25
 */
public class GoogleUserInfo {
    /** Google 사용자 고유 ID */
    private String id;
    /** 사용자 이메일 주소 */
    private String email;
    /** 사용자 이름 (display name) */
    private String name;
    /** 이메일 인증 여부 */
    private boolean verifiedEmail;

    /**
     * 사용자 정보의 유효성 검사 (id, email 필수)
     * @return id와 email이 모두 null이 아니면 true
     */
    public boolean isValid() {
        return id != null && email != null && !email.isEmpty();
    }

    /**
     * 기본 생성자
     */
    public GoogleUserInfo() {
    }

    // Getter 메서드들
    public String getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public boolean isVerifiedEmail() {
        return verifiedEmail;
    }

    // Setter 메서드들
    public void setId(String id) {
        this.id = id;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setVerifiedEmail(boolean verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }

    @Override
    public String toString() {
        return "GoogleUserInfo{id='" + id + "', email='" + email + 
               "', name='" + name + "', verifiedEmail=" + verifiedEmail + '}';
    }

    /**
     * Google ID를 기반으로 로컬 사용자 ID 생성
     * 
     * @return "google_" + googleId 형태의 사용자 ID
     */
    public String generateLocalUserId() {
        return "google_" + id;
    }
}