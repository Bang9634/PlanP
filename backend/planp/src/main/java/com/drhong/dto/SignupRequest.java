package com.drhong.dto;

/**
 * 회원가입 요청 데이터 전송 객체 (Data Transfer Object)
 * <p>
 * 클라이언트로부터 받은 회원가입 요청 데이터를 캡슐화하는 DTO 클래스이다.
 * HTTP 요청 본문의 JSON 데이터가 이 객체로 역직렬화되어 서버에서 처리된다.
 * </p>
 * 
 * <h3>JSON 요청 예시:</h3>
 * <pre>{@code
 * {
 *   "userId": "myuser123",
 *   "password": "securePassword!",
 *   "name": "홍길동",
 *   "email": "hong@example.com"
 * }
 * }</pre>
 * 
 * <h3>데이터 검증:</h3>
 * <ul>
 *   <li>모든 필드는 필수값이며 null이거나 빈 문자열이면 안된다</li>
 *   <li>userId: 영문자, 숫자, 특수문자 조합 (4-20자)</li>
 *   <li>password: 최소 8자 이상, 대소문자, 숫자, 특수문자 포함</li>
 *   <li>name: 실명 (2-50자)</li>
 *   <li>email: 유효한 이메일 형식</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.dto.SignupResponse
 * @see com.drhong.validator.SignupValidator
 */
public class SignupRequest {
    private String userId;   // 사용자 고유 식별자 (로그인 ID)
    private String password; // 사용자 비밀번호 (평문, 서버에서 해시화되어 저장)
    private String name;     // 사용자 이름
    private String email;    // 사용자 이메일

    /**
     * 기본 생성자
     * <p>
     * JSON 역직렬화 과정에서 Gson 라이브러리가 사용한다.
     * 객체 생성 후 setter 메서드를 통해 필드값이 설정된다.
     * </p>
     */
    public SignupRequest() {}

    /**
     * 전체 필드를 초기화하는 생성자
     * <p>
     * 테스트 코드나 다른 서비스에서 SignupRequest 객체를 직접 생성할 때 사용한다.
     * 모든 필드를 한 번에 설정할 수 있어 편리하다.
     * </p>
     * 
     * @param userId 사용자 ID (필수, null 불가)
     * @param password 비밀번호 (필수, null 불가)
     * @param name 사용자 이름 (필수, null 불가)
     * @param email 이메일 주소 (필수, null 불가)
     */
    public SignupRequest(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    // 게터와 세터
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }


    /**
     * <p>
     * <strong>주의:</strong> 이 메서드는 평문 비밀번호를 반환한다.
     * 로그나 디버깅 목적으로 사용할 때 보안에 주의해야 한다.
     * </p>
     * 
     * @return 사용자 비밀번호 (평문)
     */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * 객체의 문자열 표현을 반환하는 메서드
     * <p>
     * 로깅이나 디버깅 목적으로 사용된다. 
     * <strong>보안상 비밀번호는 포함되지 않는다.</strong>
     * </p>
     * 
     * @return 사용자 ID, 이름, 이메일을 포함한 문자열
     * 
     * @implNote 비밀번호는 보안상 toString()에 포함하지 않음
     */
    @Override
    public String toString() {
        return String.format("SignupRequest{userId='%s', name='%s', email='%s'}", 
                           userId, name, email );
    }
}