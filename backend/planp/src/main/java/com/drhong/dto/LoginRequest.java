package com.drhong.dto;

/**
 * 로그인 요청 데이터 전송 객체 (Data Transfer Object)
 * <p>
 * 클라이언트로부터 받은 로그인 요청 데이터를 캡슐화하는 DTO 클래스이다.
 * HTTP 요청 본문의 JSON 데이터가 이 객체로 역직렬화되어 서버에서 처리된다.
 * </p>
 * 
 * <h3>JSON 요청 예시:</h3>
 * <pre>{@code
 * {
 *   "userId": "myuser123",
 *   "password": "securePassword!"
 * }
 * }</pre>
 * 
 * <h3>데이터 검증:</h3>
 * <ul>
 *   <li>userId: 필수값, null이거나 빈 문자열이면 안된다</li>
 *   <li>password: 필수값, null이거나 빈 문자열이면 안된다</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.controller.UserController
 * @see com.drhong.service.UserService
 */
public class LoginRequest {
    private String userId;   // 사용자 ID (로그인 아이디)
    private String password; // 사용자 비밀번호 (평문, 서버에서 해시된 값과 비교)

    /**
     * 기본 생성자
     * <p>
     * JSON 역직렬화 과정에서 Gson 라이브러리가 사용한다.
     * 객체 생성 후 setter 메서드를 통해 필드값이 설정된다.
     * </p>
     */
    public LoginRequest() {}

    /**
     * 전체 필드를 초기화하는 생성자
     * <p>
     * 테스트 코드나 다른 서비스에서 LoginRequest 객체를 직접 생성할 때 사용한다.
     * 모든 필드를 한 번에 설정할 수 있어 편리하다.
     * </p>
     * 
     * @param userId 사용자 ID (필수, null 불가)
     * @param password 비밀번호 (필수, null 불가)
     */
    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    // 게터와 세터
    /**
     * 사용자 ID를 반환한다.
     * 
     * @return 사용자의 로그인 ID
     */
    public String getUserId() { 
        return userId; 
    }

    /**
     * 사용자 ID를 설정한다.
     * 
     * @param userId 설정할 사용자 ID
     */
    public void setUserId(String userId) { 
        this.userId = userId; 
    }

    /**
     * 사용자 비밀번호를 반환한다.
     * <p>
     * <strong>주의:</strong> 이 메서드는 평문 비밀번호를 반환한다.
     * 로그나 디버깅 목적으로 사용할 때 보안에 주의해야 한다.
     * </p>
     * 
     * @return 사용자 비밀번호 (평문)
     */
    public String getPassword() { 
        return password; 
    }

    /**
     * 사용자 비밀번호를 설정한다.
     * 
     * @param password 설정할 비밀번호
     */
    public void setPassword(String password) { 
        this.password = password; 
    }

    /**
     * 객체의 문자열 표현을 반환하는 메서드
     * <p>
     * 로깅이나 디버깅 목적으로 사용된다.
     * <strong>보안상 비밀번호는 포함되지 않는다.</strong>
     * </p>
     * 
     * @return 사용자 ID를 포함한 문자열 (형식: "LoginRequest{userId='값'}")
     * 
     * @implNote 비밀번호는 보안상 toString()에 포함하지 않음
     */
    @Override
    public String toString() {
        return String.format("LoginRequest{userId='%s'}", userId);
    }
}
