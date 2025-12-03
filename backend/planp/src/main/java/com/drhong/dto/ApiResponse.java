package com.drhong.dto;

import com.google.gson.Gson;

/**
 * 표준화된 API 응답 DTO
 * 
 * <p>
 * 모든 API 엔드포인트의 응답을 통일된 형식으로 제공한다.
 * </p>
 * 
 * <h3>응답 구조:</h3>
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "성공 메시지",
 *   "data": {
 *     "userId": "testuser",
 *     "email": "test@example.com"
 *   },
 *   "timestamp": 1699123456789
 * }
 * }</pre>
 */
public class ApiResponse<T> {
    private static final Gson gson = new Gson();
    
    private boolean success;
    private String message;
    private T data;
    private long timestamp;
    
    /**
     * 기본 생성자
     */
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 성공 응답 생성자
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 성공 응답 생성 (정적 팩토리 메서드)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
    
    /**
     * 실패 응답 생성 (정적 팩토리 메서드)
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    /**
     * 실패 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
    
    /**
     * JSON 문자열로 변환
     */
    public String toJson() {
        return gson.toJson(this);
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}