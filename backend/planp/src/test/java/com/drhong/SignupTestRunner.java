package com.drhong;

import com.drhong.dto.SignupRequest;
import com.drhong.dto.SignupResponse;
import com.drhong.service.UserService;

/**
 * 회원가입 로직 테스트 실행기
 */
public class SignupTestRunner {

    public static void main(String[] args) {
        System.out.println("=== 회원가입 로직 테스트 시작 ===\n");

        UserService userService = new UserService();

        // 테스트 1: 정상 회원가입
        System.out.println("1. 정상 회원가입 테스트");
        testNormalSignup(userService);

        // 테스트 2: 중복 ID 테스트
        System.out.println("\n2. 중복 ID 테스트");
        testDuplicateId(userService);

        // 테스트 3: 중복 이메일 테스트
        System.out.println("\n3. 중복 이메일 테스트");
        testDuplicateEmail(userService);

        // 테스트 4: 잘못된 입력 테스트
        System.out.println("\n4. 잘못된 입력 테스트");
        testInvalidInput(userService);

        // 테스트 5: 중복 확인 기능 테스트
        System.out.println("\n5. 중복 확인 기능 테스트");
        testDuplicateCheck(userService);

        System.out.println("\n=== 회원가입 로직 테스트 완료 ===");
    }

    private static void testNormalSignup(UserService userService) {
        SignupRequest request = new SignupRequest(
            "testuser1", "password123123", "홍길동", "hong@example.com"
        );
        SignupResponse response = userService.signup(request);
        
        System.out.println("요청: " + request);
        System.out.println("응답: " + response.toJson());
        System.out.println("결과: " + (response.isSuccess() ? "성공" : "실패"));
    }

    private static void testDuplicateId(UserService userService) {
        // 같은 ID로 재시도
        SignupRequest request = new SignupRequest(
            "testuser1", "newpassword123123", "김철수", "kim@example.com"
        );
        SignupResponse response = userService.signup(request);
        
        System.out.println("요청: " + request);
        System.out.println("응답: " + response.toJson());
        System.out.println("결과: " + (response.isSuccess() ? "성공" : "실패 (예상됨)"));
    }

    private static void testDuplicateEmail(UserService userService) {
        // 같은 이메일로 재시도
        SignupRequest request = new SignupRequest(
            "testuser2", "password456123", "이영희", "hong@example.com"
        );
        SignupResponse response = userService.signup(request);
        
        System.out.println("요청: " + request);
        System.out.println("응답: " + response.toJson());
        System.out.println("결과: " + (response.isSuccess() ? "성공" : "실패 (예상됨)"));
    }

    private static void testInvalidInput(UserService userService) {
        SignupRequest request = new SignupRequest(
            "ab", "123", "", "invalid-email"
        );
        SignupResponse response = userService.signup(request);
        
        System.out.println("요청: " + request);
        System.out.println("응답: " + response.toJson());
        System.out.println("결과: " + (response.isSuccess() ? "성공" : "실패 (예상됨)"));
    }

    private static void testDuplicateCheck(UserService userService) {
        System.out.println("testuser1 사용 가능: " + userService.isUserIdAvailable("testuser1"));
        System.out.println("newuser 사용 가능: " + userService.isUserIdAvailable("newuser"));
        System.out.println("hong@example.com 사용 가능: " + userService.isEmailAvailable("hong@example.com"));
        System.out.println("new@example.com 사용 가능: " + userService.isEmailAvailable("new@example.com"));
        System.out.println("총 사용자 수: " + userService.getUserCount());
    }
}