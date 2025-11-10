package com.drhong.dto;

/**
 * 회원가입 요청 데이터 <p>
 * 
 * 
 * @author bang9634
 */
public class SignupRequest {
    private String userId;
    private String password;
    private String name;
    private String email;

    // 기본 생성자
    public SignupRequest() {}

    // 전체 생성자
    public SignupRequest(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return String.format("SignupRequest{userId='%s', name='%s', email='%s'}", 
                           userId, name, email);
    }
}