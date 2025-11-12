package com.drhong.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 회원가입 응답 데이터 전송 객체 (Data Transfer Object)
 * <p>
 * 서버에서 클라이언트로 전송하는 회원가입 처리 결과를 캡슐화하는 DTO 클래스이다.
 * 회원가입 성공/실패 여부, 관련 메시지, 생성된 사용자 ID 등의 정보를 포함한다.
 * </p>
 * 
 * <h3>JSON 응답 예시:</h3>
 * <pre>{@code
 * // 성공 응답
 * {
 *   "success": true,
 *   "message": "회원가입이 완료되었습니다.",
 *   "userId": "myuser123"
 * }
 * 
 * // 실패 응답
 * {
 *   "success": false,
 *   "message": "이미 사용중인 사용자 ID입니다."
 * }
 * }</pre>
 * 
 * <h3>응답 타입:</h3>
 * <ul>
 *   <li><strong>성공 응답:</strong> success=true, 사용자 메시지, 생성된 userId 포함</li>
 *   <li><strong>실패 응답:</strong> success=false, 오류 메시지만 포함 (userId 제외)</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.dto.SignupRequest
 * @see com.drhong.controller.UserController#handleSignup(com.sun.net.httpserver.HttpExchange)
 */
public class SignupResponse {
    private static final Gson gson = new GsonBuilder().create();

    private boolean success; // 회원가입 처리 성공 여부
    private String message;  // 클라이언트에게 전달할 메시지
    private String userId;   // 생성된 사용자 ID, 회원가입 실패 시 null

    /**
     * 기본 생성자
     * <p>
     * JSON 직렬화/역직렬화 과정에서 Gson 라이브러리가 사용한다.
     * 객체 생성 후 setter 메서드를 통해 필드값이 설정된다.
     * </p>
     */
    public SignupResponse() {}

    /**
     * 실패 응답용 생성자
     * <p>
     * 회원가입이 실패했을 때 사용하는 생성자이다.
     * userId는 설정되지 않으므로 JSON 응답에 포함되지 않는다.
     * </p>
     * 
     * @param success 처리 결과 (일반적으로 false)
     * @param message 실패 원인을 설명하는 메시지
     * 
     * @apiNote 실패 응답 전용 - userId는 포함되지 않음
     */
    public SignupResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        // userId는 의도적으로 null로 유지 (실패 시에는 불필요)
    }

    /**
     * 성공 응답용 생성자
     * <p>
     * 회원가입이 성공했을 때 사용하는 생성자이다.
     * 모든 필드가 설정되어 완전한 성공 응답을 생성한다.
     * </p>
     * 
     * @param success 처리 결과 (일반적으로 true)
     * @param message 성공을 알리는 메시지
     * @param userId 생성된 사용자 ID
     * 
     * @apiNote 성공 응답 전용 - 모든 필드 포함
     */
    public SignupResponse(boolean success, String message, String userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }

    /**
     * 응답 객체를 JSON 문자열로 변환하는 메서드
     * <p>
     * Gson을 사용하여 안전하고 효율적인 JSON 직렬화를 수행한다.
     * null 값은 제외되고, 압축된 형태로 출력된다.
     * </p>
     * 
     * @return JSON 형태의 문자열
     * @since 1.1.0 - Gson 사용으로 업데이트
     */
    public String toJson() {
        return gson.toJson(this);
    }

    // 게터와 세터
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}