package com.drhong.dto;

/**
 * 회원가입 응답 데이터
 */
public class SignupResponse {
    private boolean success;
    private String message;
    private String userId;

    // 기본 생성자
    public SignupResponse() {}

    // 실패 응답용 생성자
    public SignupResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 성공 응답용 생성자
    public SignupResponse(boolean success, String message, String userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }

    // JSON 변환 메서드
    public String toJson() {
        if (userId != null) {
            return String.format(
                "{\"success\":%b,\"message\":\"%s\",\"userId\":\"%s\"}",
                success, message, userId
            );
        }
        return String.format(
            "{\"success\":%b,\"message\":\"%s\"}",
            success, message
        );
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}