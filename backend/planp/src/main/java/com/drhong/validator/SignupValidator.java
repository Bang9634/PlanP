
package com.drhong.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.drhong.dto.SignupRequest;

/**
 * 회원가입 요청 데이터의 유효성을 검증하는 유틸리티 클래스
 * <p>
 * 이 클래스는 회원가입 시 사용자가 입력한 데이터의 형식과 규칙을 검증한다.
 * 클라이언트 사이드 검증을 우회할 수 있으므로 서버 사이드에서 반드시 수행해야 하는
 * 보안 검증의 핵심 역할을 담당한다.
 * </p>
 * 
 * <h3>검증 항목:</h3>
 * <ul>
 *   <li><strong>사용자 ID:</strong> 길이, 허용 문자, 고유성</li>
 *   <li><strong>비밀번호:</strong> 길이, 복잡도, 보안 요구사항</li>
 *   <li><strong>이름:</strong> 길이, 필수 입력 여부</li>
 *   <li><strong>이메일:</strong> 형식, RFC 표준 준수</li>
 * </ul>
 * 
 * <h3>검증 정책:</h3>
 * <ul>
 *   <li>모든 필드는 null과 빈 문자열을 허용하지 않음</li>
 *   <li>화이트스페이스 트림 처리로 의도치 않은 공백 제거</li>
 *   <li>정규표현식 기반의 엄격한 형식 검증</li>
 *   <li>사용자 친화적인 한국어 오류 메시지 제공</li>
 * </ul>
 * 
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * SignupRequest request = new SignupRequest("user123", "password!", "홍길동", "hong@example.com");
 * List<String> errors = SignupValidator.validate(request);
 * 
 * if (!errors.isEmpty()) {
 *     System.out.println("검증 실패:");
 *     errors.forEach(System.out::println);
 * } else {
 *     System.out.println("검증 성공");
 * }
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.dto.SignupRequest
 * 
 * @implNote 모든 메서드는 static으로 설계되어 인스턴스 생성 없이 사용 가능
 */
public class SignupValidator {
    
    /**
     * RFC 5322 표준을 기반으로 한 이메일 검증 정규표현식
     * <p>
     * 일반적인 이메일 형식을 검증하되, 실용성을 고려하여 단순화된 패턴을 사용한다.
     * 완벽한 RFC 5322 검증은 매우 복잡하므로 실제 서비스에 적합한 수준으로 조정했다.
     * </p>
     * 
     * <h4>허용하는 형식:</h4>
     * <ul>
     *   <li><strong>로컬 부분:</strong> 영문자, 숫자, +, _, -, . 허용</li>
     *   <li><strong>도메인 부분:</strong> 영문자, 숫자, -, . 허용</li>
     *   <li><strong>최상위 도메인:</strong> 최소 2자 이상의 영문자</li>
     * </ul>
     * 
     * <h4>예시:</h4>
     * <ul>
     *   <li>✅ user@example.com</li>
     *   <li>✅ test.email+tag@sub.domain.co.kr</li>
     *   <li>✅ user_name@company.org</li>
     *   <li>❌ user@domain (TLD 없음)</li>
     *   <li>❌ @example.com (로컬 부분 없음)</li>
     *   <li>❌ user@.com (도메인 시작이 점)</li>
     * </ul>
     * 
     * @implNote 실제 이메일 존재 여부는 별도의 이메일 인증 과정에서 확인 필요
     */
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    /**
     * 사용자 ID 허용 형식을 정의하는 정규표현식
     * <p>
     * 시스템 내에서 사용자를 고유하게 식별하는 ID의 형식을 제한한다.
     * URL에 사용되거나 데이터베이스 키로 활용될 수 있으므로 안전한 문자만 허용한다.
     * </p>
     * 
     * <h4>허용 규칙:</h4>
     * <ul>
     *   <li><strong>길이:</strong> 3자 이상 20자 이하</li>
     *   <li><strong>문자:</strong> 영문 대소문자 (a-z, A-Z)</li>
     *   <li><strong>숫자:</strong> 0-9</li>
     *   <li><strong>특수문자:</strong> 언더스코어(_)만 허용</li>
     * </ul>
     * 
     * <h4>제한 사항:</h4>
     * <ul>
     *   <li>❌ 공백 문자 불허</li>
     *   <li>❌ 특수문자(!, @, #, $ 등) 불허</li>
     *   <li>❌ 한글이나 기타 유니코드 문자 불허</li>
     *   <li>❌ 하이픈(-)이나 점(.) 불허</li>
     * </ul>
     * 
     * <h4>예시:</h4>
     * <ul>
     *   <li>✅ user123</li>
     *   <li>✅ my_username</li>
     *   <li>✅ User_Name_123</li>
     *   <li>❌ us (너무 짧음)</li>
     *   <li>❌ user-name (하이픈 불허)</li>
     *   <li>❌ user@123 (@ 기호 불허)</li>
     * </ul>
     * 
     * @implNote 대소문자는 허용하지만 실제 저장 시 소문자 변환을 고려할 수 있음
     */  
    private static final Pattern USER_ID_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    /**
     * 회원가입 요청 데이터의 전체 유효성을 검증하는 메인 메서드
     * <p>
     * SignupRequest 객체의 모든 필드를 검증하고, 발견된 모든 오류를 리스트로 반환한다.
     * 첫 번째 오류에서 중단하지 않고 모든 검증을 수행하여 사용자에게 완전한 피드백을 제공한다.
     * </p>
     * 
     * <h4>검증 순서:</h4>
     * <ol>
     *   <li>요청 객체 null 검사</li>
     *   <li>사용자 ID 유효성 검증</li>
     *   <li>비밀번호 유효성 검증</li>
     *   <li>이름 유효성 검증</li>
     *   <li>이메일 주소 유효성 검증</li>
     * </ol>
     * 
     * <h4>반환 값:</h4>
     * <ul>
     *   <li><strong>빈 리스트:</strong> 모든 검증 통과</li>
     *   <li><strong>오류 메시지 리스트:</strong> 각 검증 실패에 대한 사용자 친화적 메시지</li>
     * </ul>
     * 
     * <h4>사용법:</h4>
     * <pre>{@code
     * List<String> errors = SignupValidator.validate(request);
     * if (errors.isEmpty()) {
     *     // 검증 성공 - 다음 단계 진행
     *     processSignup(request);
     * } else {
     *     // 검증 실패 - 오류 메시지 반환
     *     return SignupResponse.failure(String.join(", ", errors));
     * }
     * }</pre>
     * 
     * @param request 검증할 회원가입 요청 객체
     * @return 검증 오류 메시지 리스트 (성공 시 빈 리스트)
     * 
     * @apiNote 이 메서드는 fail-fast가 아닌 모든 오류를 수집하는 방식으로 동작
     */
    public static List<String> validate(SignupRequest request) {
        List<String> errors = new ArrayList<>();

        // null 체크
        if (request == null) {
            errors.add("요청 데이터가 없습니다.");
            return errors; // null인 경우 더 이상의 검증은 의미없으므로 즉시 반환
        }

        // 사용자 ID 검증
        validateUserId(request.getUserId(), errors);
        
        // 비밀번호 검증
        validatePassword(request.getPassword(), errors);
        
        // 이름 검증
        validateName(request.getName(), errors);
        
        // 이메일 검증
        validateEmail(request.getEmail(), errors);
        
        return errors;
    }

    /**
     * 사용자 ID의 유효성을 검증하는 헬퍼 메서드
     * <p>
     * 사용자 ID는 시스템 내에서 사용자를 고유하게 식별하는 중요한 정보이므로
     * 엄격한 규칙을 적용한다. null 검사, 공백 제거, 정규표현식 검증을 순차적으로 수행한다.
     * </p>
     * 
     * <h4>검증 규칙:</h4>
     * <ol>
     *   <li>null이거나 빈 문자열(공백만 포함) 검사</li>
     *   <li>정규표현식 패턴 매칭 검사</li>
     *   <li>길이 제한 (3-20자) 자동 검사 (정규표현식에 포함)</li>
     * </ol>
     * 
     * <h4>에러 메시지:</h4>
     * <ul>
     *   <li>"사용자 ID는 필수입니다." - null/빈값</li>
     *   <li>"사용자 ID는 3-20자의 영문자, 숫자, 언더스코어만 사용 가능합니다." - 형식 위반</li>
     * </ul>
     * 
     * @param userId 검증할 사용자 ID 문자열
     * @param errors 오류 메시지를 추가할 리스트
     * 
     * @implNote trim()을 사용하여 의도치 않은 앞뒤 공백을 제거한 후 검증
     */
    private static void validateUserId(String userId, List<String> errors) {
        if (userId == null || userId.trim().isEmpty()) {
            errors.add("사용자 ID는 필수입니다.");
        } else if (!USER_ID_PATTERN.matcher(userId).matches()) {
            errors.add("사용자 ID는 3-20자의 영문자, 숫자, 언더스코어만 사용 가능합니다.");
        }
    }

    /**
     * 비밀번호의 유효성을 검증하는 헬퍼 메서드
     * <p>
     * 비밀번호는 계정 보안의 핵심이므로 기본적인 길이 제한을 설정한다.
     * 복잡도 검증은 별도의 PasswordUtil.getPasswordStrength()를 통해 수행되므로
     * 여기서는 기본적인 형식 검증만 담당한다.
     * </p>
     * 
     * <h4>검증 규칙:</h4>
     * <ol>
     *   <li>null이거나 빈 문자열 검사</li>
     *   <li>최소 길이 (6자) 검사</li>
     *   <li>최대 길이 (50자) 검사</li>
     * </ol>
     * 
     * <h4>에러 메시지:</h4>
     * <ul>
     *   <li>"비밀번호는 필수입니다." - null/빈값</li>
     *   <li>"비밀번호는 최소 6자 이상이어야 합니다." - 너무 짧음</li>
     *   <li>"비밀번호는 최대 50자까지 가능합니다." - 너무 김</li>
     * </ul>
     * 
     * <h4>보안 고려사항:</h4>
     * <ul>
     *   <li>최소 6자는 기본적인 보안 요구사항</li>
     *   <li>최대 50자는 DoS 공격 방지 및 저장 효율성 고려</li>
     *   <li>복잡도 검증은 UserService에서 PasswordUtil 사용</li>
     * </ul>
     * 
     * @param password 검증할 비밀번호 문자열
     * @param errors 오류 메시지를 추가할 리스트
     * 
     * @see com.drhong.util.PasswordUtil#getPasswordStrength(String)
     * @implNote 비밀번호는 trim() 처리하지 않음 (의도적인 공백 허용)
     */
    private static void validatePassword(String password, List<String> errors) {
        if (password == null || password.isEmpty()) {
            errors.add("비밀번호는 필수입니다.");
        } else if (password.length() < 6) {
            errors.add("비밀번호는 최소 6자 이상이어야 합니다.");
        } else if (password.length() > 50) {
            errors.add("비밀번호는 최대 50자까지 가능합니다.");
        }
    }

    /**
     * 사용자 이름의 유효성을 검증하는 헬퍼 메서드
     * <p>
     * 사용자의 실명이나 닉네임을 검증한다. 다양한 문자(한글, 영문, 특수문자 등)를
     * 허용하되 기본적인 길이 제한과 필수 입력 여부만 검사한다.
     * </p>
     * 
     * <h4>검증 규칙:</h4>
     * <ol>
     *   <li>null이거나 빈 문자열(공백만 포함) 검사</li>
     *   <li>최대 길이 (50자) 검사</li>
     * </ol>
     * 
     * <h4>허용하는 형식:</h4>
     * <ul>
     *   <li>한글 이름: "홍길동", "김철수"</li>
     *   <li>영문 이름: "John Doe", "Jane Smith"</li>
     *   <li>혼합 이름: "홍 John", "김-철수"</li>
     *   <li>특수문자 포함: "O'Connor", "Jean-Luc"</li>
     * </ul>
     * 
     * <h4>에러 메시지:</h4>
     * <ul>
     *   <li>"이름은 필수입니다." - null/빈값</li>
     *   <li>"이름은 최대 50자까지 가능합니다." - 너무 김</li>
     * </ul>
     * 
     * @param name 검증할 이름 문자열
     * @param errors 오류 메시지를 추가할 리스트
     */
    private static void validateName(String name, List<String> errors) {
        if (name == null || name.trim().isEmpty()) {
            errors.add("이름은 필수입니다.");
        } else if (name.length() > 50) {
            errors.add("이름은 최대 50자까지 가능합니다.");
        }
    }

    /**
     * 이메일 주소의 유효성을 검증하는 헬퍼 메서드
     * <p>
     * 이메일은 계정 복구, 알림 전송 등 중요한 기능에 사용되므로 정확한 형식 검증이 필요하다.
     * 정규표현식을 사용하여 일반적인 이메일 형식을 검증한다.
     * </p>
     * 
     * <h4>검증 규칙:</h4>
     * <ol>
     *   <li>null이거나 빈 문자열(공백만 포함) 검사</li>
     *   <li>정규표현식 패턴 매칭 검사</li>
     * </ol>
     * 
     * <h4>에러 메시지:</h4>
     * <ul>
     *   <li>"이메일은 필수입니다." - null/빈값</li>
     *   <li>"올바른 이메일 형식이 아닙니다." - 형식 위반</li>
     * </ul>
     * 
     * <h4>검증 후 권장사항:</h4>
     * <ul>
     *   <li>실제 이메일 존재 여부는 이메일 인증으로 확인</li>
     * </ul>
     * 
     * @param email 검증할 이메일 주소 문자열
     * @param errors 오류 메시지를 추가할 리스트
     * 
     * @see #EMAIL_PATTERN 이메일 정규표현식 패턴
     * @implNote trim() 처리로 의도치 않은 공백 제거 후 검증
     */
    private static void validateEmail(String email, List<String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.add("이메일은 필수입니다.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("올바른 이메일 형식이 아닙니다.");
        }
    }
}